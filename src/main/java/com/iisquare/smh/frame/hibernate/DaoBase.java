package com.iisquare.smh.frame.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.iisquare.smh.frame.util.DPUtil;

/**
 * 数据访问对象超类
 */
public abstract class DaoBase<T> {
	
	private Class<T> entityClass;
	private boolean debug = false;
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private DaoNamingStrategy daoNamingStrategy;

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DaoNamingStrategy getDaoNamingStrategy() {
		return daoNamingStrategy;
	}

	public void setDaoNamingStrategy(DaoNamingStrategy daoNamingStrategy) {
		this.daoNamingStrategy = daoNamingStrategy;
	}

	public DaoBase(Class<T> clazz) {
		this.entityClass = clazz;
	}

	/**
	 * 获取当前Session对象
	 * @return
	 */
	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * 获取Criteria对象
	 * @return
	 */
	public Criteria createCriteria() {
		return sessionFactory.getCurrentSession().createCriteria(entityClass);
	}
	
	/**
	 * 添加实体
	 * @param t 实体对象
	 * @return 执行结果
	 */
	public boolean insert(T t) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.save(t);
		} catch(Exception e) {
			session.clear();
			if(isDebug()) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/**
	 * 删除实体
	 * @param t 实体对象
	 * @return 执行结果
	 */
	public boolean delete(T t) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.delete(t);
		} catch(Exception e) {
			session.clear();
			if(isDebug()) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 删除指定ID的记录
	 * @param id 记录的ID值
	 * @return 影响行数
	 */
	public int deleteById(Object id) {
		return deleteByField("id", id);
	}
	
	/**
	 * 根据键值对删除相应的记录
	 * @param fieldKey 字段名
	 * @param fieldValue 字段值
	 * @return 影响行数
	 */
	public int deleteByField(String fieldKey, Object fieldValue) {
		StringBuilder hb = new StringBuilder("delete from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(fieldKey);
		if(null == fieldKey) {
			hb.append(" is null");
			return executeUpdate(hb.toString(), null);
		} else {
			hb.append(" = :").append(convertParamsKey(fieldKey));
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put(fieldKey, fieldValue);
			return executeUpdate(hb.toString(), params);
		}
	}
	
	/**
	 * 根据键值对删除相应的记录
	 * @param params 键值对
	 * @return 影响行数
	 */
	public int deleteByFields(Map<String, Object> params) {
		StringBuilder hb = new StringBuilder("delete from ")
				.append(entityClass.getSimpleName())
				.append(" where 1 = 1");
		for(Map.Entry<String, Object> item : params.entrySet()) {
			hb.append(" and ");
			if(null == item.getValue()) {
				hb.append(item.getKey()).append(" is null");
			} else {
				hb.append(item.getKey()).append(" = :").append(convertParamsKey(item.getKey()));
			}
		}
		return executeUpdate(hb.toString(), params);
	}

	/**
	 * 根据主键删除相应的记录
	 * @param ids 主键值数组
	 * @return 影响行数
	 */
	public int deleteByIds(Integer[] ids) {
		return deleteByIds("id", ids);
	}
	
	/**
	 * 根据主键删除相应的记录
	 * @param keyName 主键名称
	 * @param ids 主键值数组
	 * @return 影响行数
	 */
	public int deleteByIds(String keyName, Integer[] ids) {
		return deleteByIds(keyName, DPUtil.makeIds(ids));
	}
	
	/**
	 * 根据主键删除相应的记录
	 * @param ids 主键值串，以英文逗号分隔
	 * @return 影响行数
	 */
	public int deleteByIds(String ids) {
		return deleteByIds("id", ids);
	}
	
	/**
	 * 根据主键删除相应的记录
	 * @param keyName 主键名称
	 * @param ids 主键值串，以英文逗号分隔
	 * @return 影响行数
	 */
	public int deleteByIds(String keyName, String ids) {
		StringBuilder hb = new StringBuilder("delete from ")
		.append(entityClass.getSimpleName())
		.append(" where ").append(keyName)
		.append(" in (").append(ids).append(")");
		return executeUpdate(hb.toString(), null);
	}
	
	/**
	 * 清空全部记录
	 * @return 影响行数
	 */
	public int truncate() {
		StringBuilder sb = new StringBuilder("truncate ")
				.append(T(entityClass.getSimpleName()));
		return executeSQLUpdate(sb.toString(), null);
	}

	/**
	 * 修改实体
	 * @param t 实体对象
	 * @return 执行结果
	 */
	public boolean update(T t) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.update(t);
		} catch(Exception e) {
			session.clear();
			if(isDebug()) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 获取当前实体总记录数
	 * @return 记录行数
	 */
	public int queryCount() {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName());
		return queryCount(hb.toString(), null);
	}
	
	/**
	 * 采用限定字段获取当前实体总记录数
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @return 记录行数
	 */
	public int queryCountByField(String fieldKey, Object fieldValue) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(fieldKey);
		if(null == fieldValue) {
			hb.append(" is null");
			return queryCount(hb.toString(), null);
		} else {
			hb.append(" = :").append(convertParamsKey(fieldKey));
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put(fieldKey, fieldValue);
			return queryCount(hb.toString(), params);
		}
	}
	
	/**
	 * 采用限定字段获取当前实体总记录数
	 * @param params 键值对
	 * @return 记录行数
	 */
	public int queryCountByFields(Map<String, Object> params) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where 1 = 1");
		for(Map.Entry<String, Object> item : params.entrySet()) {
			hb.append(" and ");
			if(null == item.getValue()) {
				hb.append(item.getKey()).append(" is null");
			} else {
				hb.append(item.getKey()).append(" = :").append(convertParamsKey(item.getKey()));
			}
		}
		return queryCount(hb.toString(), params);
	}
	
	/**
	 * 根据HQL语句获取记录行数
	 * @param hql HQL语句
	 * @param params 语句参数，遵循JPA规范
	 * @return 记录行数
	 */
	public int queryCount(String hql, Map<String, Object> params) {
		String regexSelectFrom = "^((?i)select)(.+)((?i)from)";
		String regexFrom = "^((?i)from)";
		String sqlCountName = "COUNT(*)";
		StringBuilder sb = new StringBuilder("$1 ").append(sqlCountName).append(" $3");
		hql = hql.replaceFirst(regexSelectFrom, sb.toString());
		sb = new StringBuilder("select ").append(sqlCountName).append(" $1");
		hql = hql.replaceFirst(regexFrom, sb.toString());
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		return DPUtil.parseInt(query.setFirstResult(0).setMaxResults(1).uniqueResult());
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param id 主键值
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObjectById(Object id) {
		return (T) sessionFactory.getCurrentSession().get(entityClass, DPUtil.parseInt(id));
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param id 主键值
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObjectById(Object id, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			criteria.add(Restrictions.idEq(DPUtil.parseInt(id)));
			return (T) criteria.setFirstResult(0).setMaxResults(1).uniqueResult();
		}
		return queryObjectById(id);
	}
	
	/**
	 * 获取实体对象
	 * @return 实体对象
	 */
	public T queryObject() {
		return queryObject(null);
	}

	/**
	 * 获取实体对象
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	public T queryObject(String mergeFields, FetchMode fetchMode) {
		return queryObject(null, mergeFields, fetchMode);
	}
	
	/**
	 * 获取实体对象
	 * @param orderBy 排序字段
	 * @return 实体对象
	 */
	public T queryObject(String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName());
		return queryObject(hb.toString(), null, orderBy);
	}

	/**
	 * 获取实体对象
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObject(String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return (T) criteria.setFirstResult(0).setMaxResults(1).uniqueResult();
		}
		return queryObject(orderBy);
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @return 实体对象
	 */
	public T queryObjectByField(String fieldKey, Object fieldValue) {
		return queryObjectByField(fieldKey, fieldValue, null);
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param params 键值对
	 * @return 实体对象
	 */
	public T queryObjectByFields(Map<String, Object> params) {
		return queryObjectByFields(params, null);
	}

	/**
	 * 根据指定字段获取实体对象
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	public T queryObjectByField(String fieldKey, Object fieldValue,
			String mergeFields, FetchMode fetchMode) {
		return queryObjectByField(fieldKey, fieldValue, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param params 键值对
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	public T queryObjectByFields(Map<String, Object> params,
			String mergeFields, FetchMode fetchMode) {
		return queryObjectByFields(params, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param orderBy 排序字段
	 * @return 实体对象
	 */
	public T queryObjectByField(String fieldKey, Object fieldValue, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(fieldKey);
		if(null == fieldValue) {
			hb.append(" is null");
			return queryObject(hb.toString(), null, orderBy);
		} else {
			hb.append(" = :").append(convertParamsKey(fieldKey));
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put(fieldKey, fieldValue); 
			return queryObject(hb.toString(), params, orderBy);
		}
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param params 键值对
	 * @param orderBy 排序字段
	 * @return 实体对象
	 */
	public T queryObjectByFields(Map<String, Object> params, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where 1 = 1");
		for(Map.Entry<String, Object> item : params.entrySet()) {
			hb.append(" and ");
			if(null == item.getValue()) {
				hb.append(item.getKey()).append(" is null");
			} else {
				hb.append(item.getKey()).append(" = :").append(convertParamsKey(item.getKey()));
			}
		}
		return queryObject(hb.toString(), params, orderBy);
	}

	/**
	 * 根据指定字段获取实体对象
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObjectByField(String fieldKey, Object fieldValue,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			criteria.add(Restrictions.eqOrIsNull(fieldKey, fieldValue));
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return (T) criteria.setFirstResult(0).setMaxResults(1).uniqueResult();
		}
		return queryObjectByField(fieldKey, fieldValue, orderBy);
	}
	
	/**
	 * 根据指定字段获取实体对象
	 * @param params 键值对
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObjectByFields(Map<String, Object> params,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for(Map.Entry<String, Object> item : params.entrySet()) {
				criteria.add(Restrictions.eqOrIsNull(item.getKey(), item.getValue()));
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return (T) criteria.setFirstResult(0).setMaxResults(1).uniqueResult();
		}
		return queryObjectByFields(params, orderBy);
	}
	
	/**
	 * 根据HQL语句获取实体对象
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @return 实体对象
	 */
	public T queryObject(String hql, Map<String, Object> params) {
		return queryObject(hql, params, null);
	}
	
	/**
	 * 根据HQL语句获取实体对象
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param orderBy 排序字段
	 * @return 实体对象
	 */
	@SuppressWarnings("unchecked")
	public T queryObject(String hql, Map<String, Object> params, String orderBy) {
		if(null != orderBy) {
			StringBuilder hb = new StringBuilder(hql)
					.append(" order by ").append(orderBy);
			hql = hb.toString();
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		return (T) query.setFirstResult(0).setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param ids 主键值数组
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(Integer[] ids) {
		return queryListByIds("id", ids, null);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param ids 主键值数组
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(Integer[] ids, String mergeFields, FetchMode fetchMode) {
		return queryListByIds("id", ids, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param ids 主键值数组
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(Integer[] ids, String orderBy) {
		return queryListByIds("id", ids, orderBy);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param ids 主键值数组
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(Integer[] ids, String orderBy, String mergeFields, FetchMode fetchMode) {
		return queryListByIds("id", ids, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值数组
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, Integer[] ids) {
		return queryListByIds(keyName, ids, null);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值数组
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, Integer[] ids, String mergeFields, FetchMode fetchMode) {
		return queryListByIds(keyName, ids, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值数组
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, Integer[] ids, String orderBy) {
		return queryListByIds(keyName, DPUtil.makeIds(ids), orderBy);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值数组
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryListByIds(String keyName, Integer[] ids,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			criteria.add(Restrictions.in(keyName, ids));
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.list();
		}
		return queryListByIds(keyName, ids, orderBy);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param ids 主键值串，以英文逗号分隔
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String ids) {
		return queryListByIds("id", ids, null);
	}
	
	/**
	 * 根据主键获取实体对象（后缀F表示FetchMode模式，解决ambiguous冲突）
	 * @param ids 主键值串，以英文逗号分隔
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIdsF(String ids, String mergeFields, FetchMode fetchMode) {
		return queryListByIds("id", ids, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值串，以英文逗号分隔
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, String ids) {
		return queryListByIds(keyName, ids, null);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值串，以英文逗号分隔
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, String ids, String mergeFields, FetchMode fetchMode) {
		return queryListByIds(keyName, ids, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值串，以英文逗号分隔
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, String ids, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(keyName)
				.append(" in (").append(ids).append(")");
		return queryList(hb.toString(), null, orderBy);
	}

	/**
	 * 根据主键获取实体对象
	 * @param keyName 主键名称
	 * @param ids 主键值串，以英文逗号分隔
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByIds(String keyName, String ids,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		return queryListByIds(keyName, DPUtil.objectArrayToIntegerArray(
				DPUtil.explode(ids, ",")), orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 获取实体对象列表
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordFirst, int recordNum) {
		return queryList(recordFirst, recordNum, null);
	}

	/**
	 * 获取实体对象列表
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordFirst, int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryList(recordFirst, recordNum, null, mergeFields, fetchMode);
	}
	
	/**
	 * 获取实体对象列表
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordFirst, int recordNum, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName());
		return queryList(hb.toString(), null, recordFirst, recordNum, orderBy);
	}

	/**
	 * 获取实体对象列表
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryList(int recordFirst, int recordNum,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.setFirstResult(recordFirst).setMaxResults(recordNum).list();
		}
		return queryList(recordFirst, recordNum, orderBy);
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordFirst, int recordNum) {
		return queryListByField(fieldKey, fieldValue, recordFirst, recordNum, null);
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param params 键值对
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordFirst, int recordNum) {
		return queryListByFields(params, recordFirst, recordNum, null);
	}

	/**
	 * 根据指定字段实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordFirst, int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryListByField(fieldKey, fieldValue, recordFirst, recordNum, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param params 键值对
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordFirst, int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryListByFields(params, recordFirst, recordNum, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordFirst, int recordNum, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(fieldKey);
		if(null == fieldValue) {
			hb.append(" is null");
			return queryList(hb.toString(), null, recordFirst, recordNum, orderBy);
		} else {
			hb.append(" = :").append(convertParamsKey(fieldKey));
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put(fieldKey, fieldValue);
			return queryList(hb.toString(), params, recordFirst, recordNum, orderBy);
		}
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param params 键值对
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordFirst, int recordNum, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where 1 = 1");
		for(Map.Entry<String, Object> item : params.entrySet()) {
			hb.append(" and ");
			if(null == item.getValue()) {
				hb.append(item.getKey()).append(" is null");
			} else {
				hb.append(item.getKey()).append(" = :").append(convertParamsKey(item.getKey()));
			}
		}
		return queryList(hb.toString(), params, recordFirst, recordNum, orderBy);
	}

	/**
	 * 根据指定字段实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordFirst, int recordNum, String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			criteria.add(Restrictions.eqOrIsNull(fieldKey, fieldValue));
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.setFirstResult(recordFirst).setMaxResults(recordNum).list();
		}
		return queryListByField(fieldKey, fieldValue, recordFirst, recordNum, orderBy);
	}
	
	/**
	 * 根据指定字段实体对象列表
	 * @param params 键值对
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryListByFields(Map<String, Object> params,
			int recordFirst, int recordNum, String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for(Map.Entry<String, Object> item : params.entrySet()) {
				criteria.add(Restrictions.eqOrIsNull(item.getKey(), item.getValue()));
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.setFirstResult(recordFirst).setMaxResults(recordNum).list();
		}
		return queryListByFields(params, recordFirst, recordNum, orderBy);
	}
	
	/**
	 * 根据HQL语句获取实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryList(String hql, Map<String, Object> params,
			int recordFirst, int recordNum) {
		return queryList(hql, params, recordFirst, recordNum, null);
	}
	
	/**
	 * 根据HQL语句获取实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param recordFirst 第一条记录行数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryList(String hql, Map<String, Object> params,
			int recordFirst, int recordNum, String orderBy) {
		if(null != orderBy) {
			StringBuilder hb = new StringBuilder(hql)
					.append(" order by ").append(orderBy);
			hql = hb.toString();
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		return query.setFirstResult(recordFirst).setMaxResults(recordNum).list();
	}
	
	/**
	 * 获取前N条实体对象列表
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordNum) {
		return queryList(0, recordNum);
	}

	/**
	 * 获取前N条实体对象列表
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryList(0, recordNum, mergeFields, fetchMode);
	}
	
	/**
	 * 获取前N条实体对象列表
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordNum, String orderBy) {
		return queryList(0, recordNum, orderBy);
	}

	/**
	 * 获取前N条实体对象列表
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryList(int recordNum, String orderBy, String mergeFields, FetchMode fetchMode) {
		return queryList(0, recordNum, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordNum) {
		return queryListByField(fieldKey, fieldValue, 0, recordNum);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param params 键值对
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params, int recordNum) {
		return queryListByFields(params, 0, recordNum);
	}

	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryListByField(fieldKey, fieldValue, 0, recordNum, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param params 键值对
	 * @param recordNum 获取记录总数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordNum, String mergeFields, FetchMode fetchMode) {
		return queryListByFields(params, 0, recordNum, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordNum, String orderBy) {
		return queryListByField(fieldKey, fieldValue, 0, recordNum, orderBy);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param params 键值对
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordNum, String orderBy) {
		return queryListByFields(params, 0, recordNum, orderBy);
	}

	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			int recordNum, String orderBy, String mergeFields, FetchMode fetchMode) {
		return queryListByField(fieldKey, fieldValue, 0, recordNum, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取前N条实体对象列表
	 * @param params 键值对
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			int recordNum, String orderBy, String mergeFields, FetchMode fetchMode) {
		return queryListByFields(params, 0, recordNum, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据HQL语句获取前N条实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param recordNum 获取记录总数
	 * @return 实体对象列表
	 */
	public List<T> queryList(String hql, Map<String, Object> params, int recordNum) {
		return queryList(hql, params, 0, recordNum);
	}
	
	/**
	 * 根据HQL语句获取前N条实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param recordNum 获取记录总数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryList(String hql, Map<String, Object> params, int recordNum, String orderBy) {
		return queryList(hql, params, 0, recordNum, orderBy);
	}
	
	/**
	 * 获取全部实体对象列表
	 * @return 实体对象列表
	 */
	public List<T> queryList() {
		return queryList(null);
	}

	/**
	 * 获取全部实体对象列表
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryList(String mergeFields, FetchMode fetchMode) {
		return queryList(null, mergeFields, fetchMode);
	}
	
	/**
	 * 获取全部实体对象列表
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryList(String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName());
		return queryList(hb.toString(), null, orderBy);
	}

	/**
	 * 获取全部实体对象列表
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryList(String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.list();
		}
		return queryList(orderBy);
	}
	
	/**
	 * 获取指定字段限制的全部实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue) {
		return queryListByField(fieldKey, fieldValue, null);
	}
	
	/**
	 * 获取指定字段限制的全部实体对象列表
	 * @param params 键值对
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params) {
		return queryListByFields(params, null);
	}
	
	/**
	 * 获取指定字段限制的全部实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			String mergeFields, FetchMode fetchMode) {
		return queryListByField(fieldKey, fieldValue, null, mergeFields, fetchMode);
	}
	
	/**
	 * 获取指定字段限制的全部实体对象列表
	 * @param params 键值对
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params,
			String mergeFields, FetchMode fetchMode) {
		return queryListByFields(params, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取全部实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByField(String fieldKey, Object fieldValue, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where ").append(fieldKey);
		if(null == fieldValue) {
			hb.append(" is null");
			return queryList(hb.toString(), null, orderBy);
		} else {
			hb.append(" = :").append(convertParamsKey(fieldKey));
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put(fieldKey, fieldValue);
			return queryList(hb.toString(), params, orderBy);
		}
	}
	
	/**
	 * 根据指定字段获取全部实体对象列表
	 * @param params 键值对
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryListByFields(Map<String, Object> params, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName())
				.append(" where 1 = 1");
		for(Map.Entry<String, Object> item : params.entrySet()) {
			hb.append(" and ");
			if(null == item.getValue()) {
				hb.append(item.getKey()).append(" is null");
			} else {
				hb.append(item.getKey()).append(" = :").append(convertParamsKey(item.getKey()));
			}
		}
		return queryList(hb.toString(), params, orderBy);
	}

	/**
	 * 根据指定字段获取全部实体对象列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryListByField(String fieldKey, Object fieldValue,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			criteria.add(Restrictions.eqOrIsNull(fieldKey, fieldValue));
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.list();
		}
		return queryListByField(fieldKey, fieldValue, orderBy);
	}
	
	/**
	 * 根据指定字段获取全部实体对象列表
	 * @param params 键值对
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryListByFields(Map<String, Object> params,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		String[] mergeFieldArray = DPUtil.explode(mergeFields, ",", " ");
		if(mergeFieldArray.length > 0) {
			Criteria criteria = createCriteria();
			for(String field : mergeFieldArray) {
				criteria.setFetchMode(field, fetchMode);
			}
			for(Map.Entry<String, Object> item : params.entrySet()) {
				criteria.add(Restrictions.eqOrIsNull(item.getKey(), item.getValue()));
			}
			for (Order order : stringToOrderList(orderBy)) {
				criteria.addOrder(order);
			}
			return criteria.list();
		}
		return queryListByFields(params, orderBy);
	}
	
	/**
	 * 根据HQL语句获取全部实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @return 实体对象列表
	 */
	public List<T> queryList(String hql, Map<String, Object> params) {
		return queryList(hql, params, null);
	}
	
	/**
	 * 根据HQL语句获取全部实体对象列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	@SuppressWarnings("unchecked")
	public List<T> queryList(String hql, Map<String, Object> params, String orderBy) {
		if(null != orderBy) {
			StringBuilder hb = new StringBuilder(hql)
					.append(" order by ").append(orderBy);
			hql = hb.toString();
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		return query.list();
	}
	
	/**
	 * 获取实体对象分页列表
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @return 实体对象列表
	 */
	public List<T> queryPage(int page, int pageSize) {
		return queryPage(page, pageSize, null);
	}

	/**
	 * 获取实体对象分页列表
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPage(int page, int pageSize, String mergeFields, FetchMode fetchMode) {
		return queryPage(page, pageSize, null, mergeFields, fetchMode);
	}
	
	/**
	 * 获取实体对象分页列表
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryPage(int page, int pageSize, String orderBy) {
		StringBuilder hb = new StringBuilder("from ")
				.append(entityClass.getSimpleName());
		return queryPage(hb.toString(), null, page, pageSize, orderBy);
	}

	/**
	 * 获取实体对象分页列表
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPage(int page, int pageSize,
			String orderBy, String mergeFields, FetchMode fetchMode) {
		if(page < 1) page = 1;
		return queryList((page - 1) * pageSize, pageSize, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @return 实体对象列表
	 */
	public List<T> queryPageByField(String fieldKey, Object fieldValue,
			int page, int pageSize) {
		return queryPageByField(fieldKey, fieldValue, page, pageSize, null);
	}
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param params 键值对
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @return 实体对象列表
	 */
	public List<T> queryPageByFields(Map<String, Object> params, int page, int pageSize) {
		return queryPageByFields(params, page, pageSize, null);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPageByField(String fieldKey, Object fieldValue,
			int page, int pageSize, String mergeFields, FetchMode fetchMode) {
		return queryPageByField(fieldKey, fieldValue, page, pageSize, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param params 键值对
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPageByFields(Map<String, Object> params,
			int page, int pageSize, String mergeFields, FetchMode fetchMode) {
		return queryPageByFields(params, page, pageSize, null, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryPageByField(String fieldKey, Object fieldValue,
			int page, int pageSize, String orderBy) {
		if(page < 1) page = 1;
		return queryListByField(fieldKey, fieldValue, (page - 1) * pageSize, pageSize, orderBy);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param params 键值对
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryPageByFields(Map<String, Object> params,
			int page, int pageSize, String orderBy) {
		if(page < 1) page = 1;
		return queryListByFields(params, (page - 1) * pageSize, pageSize, orderBy);
	}

	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param fieldKey 字段名称
	 * @param fieldValue 字段值
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPageByField(String fieldKey, Object fieldValue,
			int page, int pageSize, String orderBy, String mergeFields, FetchMode fetchMode) {
		if(page < 1) page = 1;
		return queryListByField(fieldKey, fieldValue, (page - 1) * pageSize, pageSize, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据指定字段获取实体对象分页列表
	 * @param params 键值对
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @param mergeFields 急切加载的字段，以英文逗号分隔
	 * @param fetchMode 关联查询模式
	 * @return 实体对象列表
	 */
	public List<T> queryPageByFields(Map<String, Object> params,
			int page, int pageSize, String orderBy, String mergeFields, FetchMode fetchMode) {
		if(page < 1) page = 1;
		return queryListByFields(params, (page - 1) * pageSize, pageSize, orderBy, mergeFields, fetchMode);
	}
	
	/**
	 * 根据HQL语句获取实体对象分页列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @return 实体对象列表
	 */
	public List<T> queryPage(String hql, Map<String, Object> params,
			int page, int pageSize) {
		return queryPage(hql, params, page, pageSize, null);
	}
	
	/**
	 * 根据HQL语句获取实体对象分页列表
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @param page 当前页数
	 * @param pageSize 每页记录条数
	 * @param orderBy 排序字段
	 * @return 实体对象列表
	 */
	public List<T> queryPage(String hql, Map<String, Object> params,
			int page, int pageSize, String orderBy) {
		if(page < 1) page = 1;
		return queryList(hql, params, (page - 1) * pageSize, pageSize, orderBy);
	}
	
	/**
	 * 设置查询语句参数，遵循JPA规范
	 * @param query 查询对象
	 * @param params 查询参数
	 */
	public void setQueryParams(Query query, Map<String, Object> params) {
		if (null == params) {
			return;
		}
		for(Map.Entry<String, Object> item : params.entrySet()) {
			query.setParameter(convertParamsKey(item.getKey()), item.getValue());
		}
	}
	
	/**
	 * 根据HQL语句创建Query对象
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @return
	 */
	public Query createQuery(String hql, Map<String, Object> params) {
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		return query;
	}
	
	/**
	 * 执行HQL语句
	 * @param hql HQL语句
	 * @param params 语句参数
	 * @return 影响行数，执行失败时返回-1
	 */
	public int executeUpdate(String hql, Map<String, Object> params) {
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		setQueryParams(query, params);
		try {
			return query.executeUpdate();
		} catch(Exception e) {
			if(isDebug()) {
				e.printStackTrace();
			}
			return -1;
		}
	}
	
	/**
	 * 根据SQL语句创建Query对象
	 * @param sql SQL语句
	 * @param params 语句参数
	 * @return
	 */
	public Query createSQLQuery(String sql, Map<String, Object> params) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		setQueryParams(query, params);
		return query;
	}
	
	/**
	 * 执行SQL语句
	 * @param sql SQL语句
	 * @param params 语句参数
	 * @return 影响行数，执行失败时返回-1
	 */
	public int executeSQLUpdate(String sql, Map<String, Object> params) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		setQueryParams(query, params);
		try {
			return query.executeUpdate();
		} catch(Exception e) {
			if(isDebug()) {
				e.printStackTrace();
			}
			return -1;
		}
	}
	
	/**
	 * 将字符串转换为org.hibernate.criterion.Order列表
	 * @param string
	 * @return
	 */
	public List<Order> stringToOrderList(String orderBy) {
		List<Order> list = new ArrayList<Order>(0);
		for(String order : DPUtil.explode(orderBy, ",", " ")) {
			String[] strs = order.split("\\s+");
			if (1 == strs.length
					|| "asc".equals(strs[1].toLowerCase())) {
				list.add(Order.asc(strs[0]));
			} else {
				list.add(Order.desc(strs[0]));
			}
			
		}
		return list;
	}
	
	/**
	 * 将SQL中=：参数中的.替换为伪代码的合法命名
	 * @param key
	 * @return
	 */
	public String convertParamsKey(String key) {
		return key.replaceAll("\\.", "_");
	}
	
	/**
	 * 将实体类名称转换为数据库表名称
	 * @param className 实体类名称
	 * @return 数据库表名称
	 */
	public String T(String className) {
		StringBuilder sb = new StringBuilder(daoNamingStrategy.getTablePrefix())
				.append(DPUtil.addUnderscores(StringHelper.unqualify(className)));
		 return sb.toString();
	}
	
	/**
	 * 将实体属性名称转换为字段名称
	 * @param columnName 实体属性名称
	 * @return 数据库字段名称
	 */
	public String C(String propertyName) {
		return DPUtil.addUnderscores(StringHelper.unqualify(propertyName));
	}
	
	/**
	 * 获取当前DAO对象的表名
	 * @return
	 */
	public String tableName() {
		return T(entityClass.getSimpleName());
	}
}
