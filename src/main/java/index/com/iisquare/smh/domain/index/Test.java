package com.iisquare.smh.domain.index;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 测试实体类
 * @author Ouyang
 *
 */
@Entity
@DynamicInsert
@DynamicUpdate
public class Test {
	@Id
	@GeneratedValue
	private Integer id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentId")
	private Test parent;
	private String name;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Test getParent() {
		return parent;
	}
	
	/**
	 * 采用数值0代替外键null避免全表扫描
	 * 此时对应的字段应该设置了not null default 0
	 * @param parent
	 */
	public void setParent(Test parent) {
		if(null != parent && 0 == parent.getId()) {
			this.parent = null;
		} else {
			this.parent = parent;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Test() {
		
	}

	@Override
	public boolean equals(Object obj) {
		if(null != obj && obj instanceof Test
				&& id.equals(((Test) obj).getId())) return true;
		return false;
	}
}
