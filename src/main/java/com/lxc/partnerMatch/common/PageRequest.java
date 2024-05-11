package com.lxc.partnerMatch.common;

import lombok.Data;

import java.io.Serializable;

/**
 *  通过分页请求参数
 * @author mortal
 * @date 2024/4/26 18:32
 */
@Data
// 序列化的过程就是将 Java 对象转换为字节流的过程，这样可以将对象保存到文件中、在网络上传输或者以其他方式进行持久化。
public class PageRequest implements Serializable {

// 无论类的结构是否发生变化，序列化ID都不会改变，从而确保了对象在序列化和反序列化过程中的一致性。
	// 这个序列化ID是一个唯一的标识符，用于标识对象的版本，它在对象序列化和反序列化过程中起到重要的作用。
	private static final long serialVersionUID = 3515010070104563381L;
	/**
	 *  当前页码
	 */
	// 用 protected 修饰的成员只能在同一个包中的其他类，或者该类的子类中被访问。
	protected int pageNum;
	/**
	 *  当前页面的大小（多少记录数）
	 */
	protected int pageSize;
}
