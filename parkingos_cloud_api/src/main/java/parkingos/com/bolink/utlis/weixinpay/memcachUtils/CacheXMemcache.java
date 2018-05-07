package parkingos.com.bolink.utlis.weixinpay.memcachUtils;

import net.rubyeye.xmemcached.CASOperation;
import net.rubyeye.xmemcached.Counter;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import parkingos.com.bolink.utlis.Check;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * XMemcached缓存类
 */
public class CacheXMemcache<T> {
	
	//private final Logger logger = Logger.getLogger(CacheXMemcache.class);
	
	private MemcachedClient memcachedClient;

	/**
	 * 构造函数
	 */
	public CacheXMemcache() {
	}

	/**
	 * set注入
	 */
	public void setMemcachedClient(MemcachedClient memcachedClient) {
		memcachedClient.setConnectTimeout(1000);
		this.memcachedClient = memcachedClient;
	}
	
	/**
	 * 执行一个任务，如果在缓存中有对应的值，那么直接返回，否则执行任务并把输出保存入缓存
	 * @param task 任务
	 * @return 任务返回值
	 */
	public T doCachedTask(CachedTask<T> task) {
		String key = task.getKey();
		String flag = task.getFlag();
		T value = null;
		try {
			value = getCached(key);
			if (value == null) {//第一次创建
				value = task.run();
				setCached(key,value);
			} else if (flag != null) {//更新
				value = task.run();
				putCachedXM(key, value);
			}
		} catch (Exception e) {
			value = task.run();
			//e.printStackTrace();
		}
		return value;
	}

	/**
	 * CAS操作 实现原子更新
	 */
	public void putCachedXM(String key, final Object object) throws TimeoutException,
			InterruptedException, MemcachedException {
		memcachedClient.cas(key, new CASOperation<Object>() {
			public int getMaxTries() {
				return Integer.MAX_VALUE;
			}

			public Object getNewValue(long currentCAS, Object currentValue) {
				return object;
			}
		});
	}

	/**
	 * 得到缓存
	 * @throws MemcachedException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public T getCached(String key){
		if(Check.isEmpty(key))
			return null;
		T t = null;
		try {
			t = memcachedClient.get(key);
		} catch (TimeoutException e) {
			//logger.error(e);
		} catch (InterruptedException e) {
			//logger.error(e);
		} catch (MemcachedException e) {
			//logger.error(e);
		}
		return t;
	}
	
	/**
	 * 更新缓存
	 * @throws MemcachedException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public boolean setCached(String key,Object object){
		boolean isSuccuess = false;
		try {
			 isSuccuess = memcachedClient.set(key,0,object);
		} catch (TimeoutException e) {
			//logger.error(e);
		} catch (InterruptedException e) {
			//logger.error(e);
		} catch (MemcachedException e) {
			//logger.error(e);
		}
		return isSuccuess;
	}
	
	/**
	 * add方法，成功返回true，当数据已经存在时，返回false
	 * @param key
	 * @param object
	 * @param exp 过期时间（秒）
	 * @return
	 */
	public boolean addCached(String key, Object object, int exp){
		try {
			return memcachedClient.add(key, exp, object);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * set命令不但可以简单添加，如果set的key已经存在，该命令可以更新该key所对应的原来的数据，也就是实现更新的作用。
	 * @param key
	 * @param object
	 * @param exp
	 * @return
	 */
	public boolean setCached(String key, Object object, int exp){
		try {
			return memcachedClient.set(key, exp, object);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean delCached(String key){
		try {
			return memcachedClient.delete(key);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return false;
	}
	

	/**
	 * 计数器
	 * @throws MemcachedException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	public Counter getCounter(String key){
		return memcachedClient.getCounter(key,0);
	}
	
	/**
	 * 关闭连接
	 */
	public void shutdown() {
		try {
			memcachedClient.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
