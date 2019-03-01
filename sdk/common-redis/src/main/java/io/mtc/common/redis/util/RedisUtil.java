package io.mtc.common.redis.util;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.constants.Constants;
import io.mtc.common.redis.constants.BchRedisKeys;
import io.mtc.common.redis.constants.BtcRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author Chinhin
 * 2018/6/20
 */
@Slf4j
@Component
public class RedisUtil {


    /**
     * 根据缓存判断是否是平台用户，对应存的位置是 facade-api:service:EthService:setLanguage(Method)
     * @param walletAddress 钱包地址
     * @return true表示是，反之亦然
     */
    public boolean isPlatformUserBitcoin(String walletAddress, BitcoinTypeEnum bitcoinType) {
        if (bitcoinType == BitcoinTypeEnum.BTC) {
            return get(BtcRedisKeys.PLATFORM_USER(walletAddress)) != null;
        } else {
            return get(BchRedisKeys.PLATFORM_USER(walletAddress)) != null;
        }
    }

    @Resource(name="redisTemplate")
    private RedisTemplate<String, Object> objRedisTemplate;

    @Resource(name="stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "jedisPool")
    private JedisPool jedisPool;

    /**
     * 设置一个值
     * @param key 键
     * @param value 值,对象需要实现序列化
     * @param seconds 过期时间,秒
     */
    public void set(String key, Object value, int seconds) {
        objRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }
    public void set(String key, Object value) {
        objRedisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        Object o = objRedisTemplate.opsForValue().get(key);
        if (o == null) {
            return null;
        } else {
            return o;
        }
    }

    public <T> T get(String key, Class<T> tClass) {
        Object o = get(key);
        if (o == null) {
            return null;
        } else {
            return (T) o;
        }
    }

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * 获取分布式锁
     * 注意搭配finally，在里面删除该锁
     * @param key 锁名称
     * @param expireSeconds 自动释放时间
     * @return true表示获取成功，false表示获取失败
     */
    public Boolean distributeLock(String key, int expireSeconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String result = jedis.set(key, Constants.EMPTY, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireSeconds);
            return LOCK_SUCCESS.equals(result);
        } catch (Exception e) {
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 重新设置超时时间
     * @param key key
     * @param expireSeconds 新的超时时间
     */
    public void delayExpireTime(String key, int expireSeconds) {
        objRedisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 删除某个key
     * @param key key名
     */
    public void delete(String key) {
        objRedisTemplate.delete(key);
    }

    /**
     * 返回所有以 key 开头的key
     * @param prefix 开头的key
     * @return true表示存在
     */
    public Set<String> getKeysBeginWith(String prefix) {
        Set<String> keys = objRedisTemplate.keys(prefix + "*");
        if (keys == null) {
            return null;
        }
        return keys;
    }

    /**
     * 订阅模式发布消息
     * @param channel 频道
     * @param message 消息对象
     */
    public void pubMsg(String channel, Object message) {
        objRedisTemplate.convertAndSend(channel, message);
    }

    /**
     * 添加到队列
     * @param key 队列的key
     * @param value 值
     */
    public void lPush(String key, Object value) {
        ListOperations<String, Object> listOperations = objRedisTemplate.opsForList();
        listOperations.leftPush(key, value);
    }

    /**
     * 从队列取出
     * @param key 队列的key
     * @param seconds 时间
     * @return 取出的值
     */
    public Object brpop(String key, int seconds) {
        ListOperations<String, Object> list = objRedisTemplate.opsForList();
        return list.rightPop(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 从队列取值（无超时）
     * @param key 队列的key
     * @return 结果
     */
    public Object rPop(String key) {
        ListOperations<String, Object> list = objRedisTemplate.opsForList();
        return list.rightPop(key);
    }

    /**
     * 获取队列的长度
     * @param key 队列的key
     * @return 取出的值
     */
    public Long size(String key) {
        ListOperations<String, Object> list = objRedisTemplate.opsForList();
        return list.size(key);
    }


    /**
     * 增加／减少 原子操作
     * @return 操作后的值
     */
    public double incrby(String key, double value){
        return stringRedisTemplate.boundValueOps(key).increment(value);
    }

    /**
     * 增加／减少 原子操作
     * @return 操作后的值
     */
    public long incrby(String key, long value){
        return stringRedisTemplate.boundValueOps(key).increment(value);
    }

    /**
     * 添加大set集合
     * @return 添加的数量
     */
    public long sadd(String key, String... values){
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取集合的数量
     */
    public long scount(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * 集合中是否存在
     */
    public boolean sExist(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 返回集合
     */
    public Set<String> smembers(String key){
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * 关键字查找
     * @param key
     * @param keyword
     * @return
     */
    public List<String> sscan(String key,String keyword){
        Cursor<String> scan = stringRedisTemplate.opsForSet().scan(key, ScanOptions.scanOptions().match(keyword).count(Integer.MAX_VALUE).build());
        List<String> retlist = new ArrayList();
        while(scan.hasNext()){
            retlist.add(scan.next());
        }
        return retlist;
    }

    /**
     * 保存字符串
     */
    public void setString(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 保存map字符串键值对
     */
    public void hsetString(String key,String hkey,String value){
        stringRedisTemplate.opsForHash().put(key, hkey, value);
    }

    /**
     * 删除map中的一个属性
     */
    public void hdel(String key, String hkey) {
        stringRedisTemplate.opsForHash().delete(key, hkey);
    }

    /**
     * 获取map字符串值
     */
    public Object hgetString(String key,String hkey){
        return stringRedisTemplate.opsForHash().get(key, hkey);
    }

    /**
     * map中这个key是否存在
     */
    public Boolean hExist(String key, String hKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, hKey);
    }

    public long hKeyCount(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    /**
     * 获取map
     */
    public Map hget(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 对map的值做原子增减
     */
    public long incrby(String key, String hkey, long value){
        return stringRedisTemplate.opsForHash().increment(key, hkey, value);
    }

    /**
     * 对map的值做原子增减
     */
    public double incrby(String key, String hkey, double value){
        return stringRedisTemplate.opsForHash().increment(key, hkey, value);
    }

    /**
     * 修改名称
     */
    public void rename(String key, String newkey){
        stringRedisTemplate.rename(key, newkey);
    }

    /**
     * 移除并返回集合中的一个随机元素
     */
    public String spop(String key){
        return stringRedisTemplate.opsForSet().pop(key);
    }

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略
     */
    public long srem(String key, String... member){
        return stringRedisTemplate.opsForSet().remove(key, member);
    }

    /**
     * 返回集合 key 的基数(集合中元素的数量)
     */
    public long scard(String key){
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * 设置hash
     */
    public void hsetObj(String key, String hkey, Object value){
        objRedisTemplate.opsForHash().put(key, hkey, value);
    }

    /**
     * 设置hash，如果属性存在则不会覆盖并返回false
     */
    public boolean hsetnxString(String key, String hkey, String value){
        return stringRedisTemplate.opsForHash().putIfAbsent(key, hkey, value);
    }

    /**
     * 获取hash
     */
    public Object hgetObj(String key, String hkey){
        return objRedisTemplate.opsForHash().get(key, hkey);
    }

}
