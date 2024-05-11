package com.lxc.partnerMatch.service;

import com.lxc.partnerMatch.utils.AlgorithmUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具 测试类
 *
 * @author mortal
 * @date 2024/5/6 23:59
 */
@SpringBootTest
public class AlgorithmUtilsTest {
	@Test
	void testCompareTags() {
		// 最小编辑距离：字符串 1 通过最少多少次增删改字符的操作可以变成字符串 2
		// 知道用户的标签集合，我们就知道哪些用户更相似（数值越小，相似度越高）
		List<String> tagList1 = Arrays.asList("Java", "大一", "男");
		List<String> tagList2 = Arrays.asList("Java", "大一", "女");
		List<String> tagList3 = Arrays.asList("Python", "大二", "男");

		// 1
		int score1 = AlgorithmUtil.minDistance(tagList1, tagList2);
		System.out.println(score1);
		// 2
		int score2 = AlgorithmUtil.minDistance(tagList1, tagList3);
		System.out.println(score2);
	}


	@Test
	void testAlgorithm() {
		// 最小编辑距离：字符串 1 通过最少多少次增删改字符的操作可以变成字符串 2
		String str1 = "李小成是帅哥";
		String str2 = "李小成不是帅哥";
		String str3 = "李小成超帅的";

		//对于 str1 和 str2，从 str1 转换到 str2，可以通过一次删除操作（删除 "不是"），
		// 从而使得两个字符串相同，所以最小编辑距离是 1。
		int score1 = AlgorithmUtil.minDistance(str1, str2);
		System.out.println(score1);

		int score2 = AlgorithmUtil.minDistance(str1, str3);
		System.out.println(score2);
	}
}
