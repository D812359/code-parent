package com.tom.controller;

import com.tom.domain.MiaoshaOrder;
import com.tom.domain.MiaoshaUser;
import com.tom.domain.OrderInfo;
import com.tom.rabbitmq.MQSender;
import com.tom.rabbitmq.MiaoshaMessage;
import com.tom.redis.GoodsKey;
import com.tom.redis.MiaoshaKey;
import com.tom.redis.OrderKey;
import com.tom.redis.RedisService;
import com.tom.result.CodeMsg;
import com.tom.result.Result;
import com.tom.service.GoodsService;
import com.tom.service.MiaoshaService;
import com.tom.service.OrderService;
import com.tom.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.security.auth.login.CredentialException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: Tom
 * @date: 2020-02-28 15:11
 * @description: 秒杀控制器
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    MiaoshaService miaoshaService;


    @Autowired
    MQSender mqSender;

    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();

        if (goodsList == null) {
            return;
        }

        //系统启动的时候把商品加载到缓存中
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    //QPS: 本机 1685-2087
    // 5000 * 10
    /*@RequestMapping(value = "/do_miaosha")
    public String miaosha(Model model, MiaoshaUser user,
                          @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);

        if (user == null) {
            return "login";
        }

        //判断商品库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }

        //判断是否已经秒杀到
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (miaoshaOrder != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }

        //1、减库存，2、下订单，3、写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        model.addAttribute("goods", goods);
        model.addAttribute("orderInfo", orderInfo);
        return "order_detail";
    }*/

    /**
     * 优化版一：
     * 优化思路（解决并发大的问题-瓶颈就在数据库）：最有效的解决方法-加缓存
     * 1、浏览器做页面静态化，可以直接把页面缓存到用户的浏览器端
     * 2、请求到达网站之前，可以部署CDN节点，
     * 3、到达网站时，可以在Nginx加缓存
     * 4、页面缓存
     * 5、对象缓存
     * 6、数据库
     * 通过访问不同粒度，不同层面的缓存，逐步削减访问数据库的数量。
     * GET POST 区别？
     * GET 幂等：从服务端获取数据，无论掉多少次 产生的结果是一样的，不会对服务端数据产生影响
     * POST 向服务端提交数据
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    //QPS: 本机 1816-2097
    // 5000 * 10
    /*@RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> miaosha(Model model, MiaoshaUser user,
                                     @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //判断商品库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();

        if (stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (miaoshaOrder != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        //1、减库存，2、下订单，3、写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);

        return Result.success(orderInfo);
    }*/

    /**
     * 优化版二：
     * 1、系统初始化，商品库存加载到 Redis
     * 2、收到请求，Redis预减库存，库存不足，直接返回，否则进入3
     * 3、异步下单，请求入队，立即返回排队中
     * 4、请求出队，生成订单，减少库存
     * 5、客户端轮询，是否秒杀成功
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //内存标记，减少内存访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);

        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        MiaoshaMessage m = new MiaoshaMessage();
        m.setUser(user);
        m.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(m);

        return Result.success(0); //0-排队中
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * 数据还原
     * @param model
     * @return
     */
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

}
