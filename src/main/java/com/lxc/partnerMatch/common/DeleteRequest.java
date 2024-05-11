package com.lxc.partnerMatch.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通过 的 删除请求  的参数
 *
 * @author mortal
 * @date 2024/4/26 18:32
 */
@Data
// 序列化的过程就是将 Java 对象转换为字节流的过程，这样可以将对象保存到文件中、在网络上传输或者以其他方式进行持久化。
public class DeleteRequest implements Serializable {


	// 无论类的结构是否发生变化，序列化ID都不会改变，从而确保了对象在序列化和反序列化过程中的一致性。
	// 这个序列化ID是一个唯一的标识符，用于标识对象的版本，它在对象序列化和反序列化过程中起到重要的作用。
	private static final long serialVersionUID = -1599760475969755834L;
	/**
	 * 要删除的 id
	 */

	private long id;
}
