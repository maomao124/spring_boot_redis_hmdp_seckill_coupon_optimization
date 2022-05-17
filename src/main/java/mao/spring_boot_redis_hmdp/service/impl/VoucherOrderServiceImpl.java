package mao.spring_boot_redis_hmdp.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mao.spring_boot_redis_hmdp.dto.Result;
import mao.spring_boot_redis_hmdp.entity.SeckillVoucher;
import mao.spring_boot_redis_hmdp.entity.VoucherOrder;
import mao.spring_boot_redis_hmdp.mapper.VoucherOrderMapper;
import mao.spring_boot_redis_hmdp.service.ISeckillVoucherService;
import mao.spring_boot_redis_hmdp.service.IVoucherOrderService;
import mao.spring_boot_redis_hmdp.utils.RedisIDGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService
{
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIDGenerator redisIDGenerator;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    /**
     * 阻塞队列
     */
    private BlockingQueue<VoucherOrder> blockingQueue = new ArrayBlockingQueue<>(1024 * 1024);

    /**
     * 线程池
     */
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    static
    {
        //创建对象
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        //加载
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        //设置返回类型
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    private void init()
    {
        SECKILL_ORDER_EXECUTOR.submit(() ->
        {
            new VoucherOrderHandler();
        });
    }

    private class VoucherOrderHandler implements Runnable
    {
        @Override
        public void run()
        {
            //系统启动开始，便不断从阻塞队列中获取优惠券订单信息
            while (true)
            {
                try
                {
                    //获取订单信息

                }
                catch (Exception e)
                {

                }
            }
        }
    }


//    @Override
//    public Result seckillVoucher(Long voucherId)
//    {
//        //查询优惠券
//        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
//        //判断是否存在
//        if (seckillVoucher == null)
//        {
//            return Result.fail("活动不存在");
//        }
//        //判断是否开始
//        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now()))
//        {
//            //未开始
//            return Result.fail("秒杀活动未开始");
//        }
//        //判断是否结束
//        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now()))
//        {
//            //结束
//            return Result.fail("秒杀活动已经结束");
//        }
//        //判断库存是否充足
//        if (seckillVoucher.getStock() <= 0)
//        {
//            //库存不足
//            return Result.fail("库存不足");
//        }
//        //创建订单
//        return this.createVoucherOrder(voucherId);
//    }
//
//
//    /**
//     * 创建订单
//     *
//     * @param voucherId voucherId
//     * @return Result
//     */
//    @Transactional
//    public Result createVoucherOrder(Long voucherId)
//    {
//        //判断当前优惠券用户是否已经下过单
//        //获得用户id
//        //Long userID = UserHolder.getUser().getId();
//        Long userID = 5L;
//        //todo:记得更改回来
//        /*//创建锁对象
//        RedisLock redisLock = new RedisLockImpl("order:" + userID, stringRedisTemplate);
//        //取得锁
//        boolean isLock = redisLock.tryLock(1500);
//        //判断
//        if (!isLock)
//        {
//            return Result.fail("不允许重复下单！");
//        }*/
//
//
//        //redisson锁
//        RLock lock = redissonClient.getLock("lock:order:" + userID);
//        //尝试获取锁
//        //仅当调用时它是空闲的时才获取锁。
//        //如果锁可用，则获取锁并立即返回值为true
//        //如果锁不可用，则此方法将立即返回值false
//        boolean tryLock = lock.tryLock();
//        //判断是否获取成功
//        if (!tryLock)
//        {
//            return Result.fail("不允许重复下单！");
//        }
//        //获取锁成功
//        //synchronized (userID.toString().intern())
//        try
//        {
//            //查询数据库
//            Long count = this.query().eq("user_id", userID).eq("voucher_id", voucherId).count();
//            //判断长度
//            if (count > 0)
//            {
//                //长度大于0，用户购买过
//                return Result.fail("不能重复下单");
//            }
//            //扣减库存
//            UpdateWrapper<SeckillVoucher> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock", 0);
//            boolean update = seckillVoucherService.update(updateWrapper);
//            if (!update)
//            {
//                //失败
//                return Result.fail("库存扣减失败");
//            }
//            //扣减成功
//            //生成订单
//            VoucherOrder voucherOrder = new VoucherOrder();
//            //生成id
//            Long orderID = redisIDGenerator.nextID("order");
//            voucherOrder.setVoucherId(voucherId);
//            voucherOrder.setId(orderID);
//            //设置用户
//            //Long userID = UserHolder.getUser().getId();
//            voucherOrder.setUserId(userID);
//            //保存订单
//            this.save(voucherOrder);
//            //返回
//            return Result.ok(orderID);
//        }
//        finally
//        {
//            //释放锁
//            //redisLock.unlock();
//            lock.unlock();
//        }
//    }


    @Override
    public Result seckillVoucher(Long voucherId)
    {

    }

    /**
     * 创建订单
     * @param voucherOrder VoucherOrder
     */
    private void createVoucherOrder(VoucherOrder voucherOrder)
    {
        //判断当前优惠券用户是否已经下过单
        //获得用户id
        //Long userID = UserHolder.getUser().getId();
        Long userID = 5L;
        //todo:记得更改回来
        /*//创建锁对象
        RedisLock redisLock = new RedisLockImpl("order:" + userID, stringRedisTemplate);
        //取得锁
        boolean isLock = redisLock.tryLock(1500);
        //判断
        if (!isLock)
        {
            return Result.fail("不允许重复下单！");
        }*/


        //redisson锁
        RLock lock = redissonClient.getLock("lock:order:" + userID);
        //尝试获取锁
        //仅当调用时它是空闲的时才获取锁。
        //如果锁可用，则获取锁并立即返回值为true
        //如果锁不可用，则此方法将立即返回值false
        boolean tryLock = lock.tryLock();
        //判断是否获取成功
        if (!tryLock)
        {
            return;
        }
        //获取锁成功
        //synchronized (userID.toString().intern())
        Long voucherId = voucherOrder.getVoucherId();
        try
        {
            //查询数据库
            Long count = this.query().eq("user_id", userID).eq("voucher_id", voucherId).count();
            //判断长度
            if (count > 0)
            {
                //长度大于0，用户购买过
                return;
            }
            //扣减库存
            UpdateWrapper<SeckillVoucher> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock", 0);
            boolean update = seckillVoucherService.update(updateWrapper);
            if (!update)
            {
                //失败
                return ;
            }
            //扣减成功
            //保存订单
            this.save(voucherOrder);
        }
        finally
        {
            //释放锁
            //redisLock.unlock();
            lock.unlock();
        }
    }
}