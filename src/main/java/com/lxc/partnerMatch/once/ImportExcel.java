package com.lxc.partnerMatch.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author mortal
 * @date 2024/4/13 17:05
 */
public class ImportExcel {
	public static void main(String[] args) {
		// 写法1：JDK8+ ,不用额外写一个PlanetTableUserInfoListener
		// since: 3.0.0-beta1
		String fileName = "C:\\Users\\lxc\\IdeaProjects\\planetProject\\partnerMatch-backend\\src\\main\\resources\\testExcel.xlsx";

		synchronousRead(fileName);
	}

	/**
	 *  同步读
	 * @param fileName
	 */
	public static void synchronousRead(String fileName) {

		// 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
		List<PlanetTableUserInfo> userInfoList = EasyExcel.read(fileName).head(PlanetTableUserInfo.class).sheet().doReadSync();

		System.out.println("总数 = " + userInfoList.size());
//使用 Java 8 中的流式操作和 Collectors.groupingBy() 方法来对 userInfoList 集合中的元素进行分组操作，
// 将相同用户名的元素分组到一个列表中，最终生成一个映射（Map）对象。
		// Map 的键是用户名，值是对应的 PlanetTableUserInfo 列表。
		Map<String, List<PlanetTableUserInfo>> listMap =
				userInfoList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(PlanetTableUserInfo::getUsername));
		// 将 listMap.keySet().size() 理解为判断 listMap 中不重复的键的数量。
		// map集合的键就是唯一的，但是我们用keySet()方法单独把键变成一个set集合
		// .keySet() 方法返回一个 Set 集合
		System.out.println("不重复的昵称数：" + listMap.keySet().size());
	}

	/**
	 *  监听器读
	 * @param fileName
	 */
	private static void readByListener(String fileName) {
		EasyExcel.read(fileName, PlanetTableUserInfo.class, new TableListener()).sheet().doRead();
	}
}