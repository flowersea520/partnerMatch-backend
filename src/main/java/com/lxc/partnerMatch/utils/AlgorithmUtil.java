package com.lxc.partnerMatch.utils;

import java.util.List;

/**
 * 算法工具类
 *
 * @author mortal
 * @date 2024/5/7 0:02
 */
public class AlgorithmUtil {


	/**
	 * 编辑距离算法（用于计算最相似的两组标签）
	 * 文档地址：https://blog.csdn.net/DBC_121/article/details/104198838
	 *
	 * @param tagList1 用户标签的集合（通过将json字符串转换为 list集合对象）
	 * @param tagList2
	 * @return 最小编辑距离：字符串 1 通过最少多少次增删改字符的操作可以变成字符串 2
	 */
	public static int minDistance(List<String> tagList1, List<String> tagList2) {
		int m = tagList1.size();
		int n = tagList2.size();

		// 构建二维数组，dp[i][j] 表示将 tagList1 的前 i 个标签转换成 tagList2 的前 j 个标签所需的最小编辑次数
		int[][] dp = new int[m + 1][n + 1];

		// 初始化边界条件
		for (int i = 0; i <= m; i++) {
			dp[i][0] = i; // tagList1 的前 i 个标签转换成空列表，需要 i 次删除操作
		}
		for (int j = 0; j <= n; j++) {
			dp[0][j] = j; // 空列表转换成 tagList2 的前 j 个标签，需要 j 次插入操作
		}

		// 填充数组
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (tagList1.get(i - 1).equals(tagList2.get(j - 1))) {
					// 如果当前标签相同，则不需要编辑操作
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					// 否则，选择插入、删除或替换中成本最小的操作
					dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], // 替换
							Math.min(dp[i - 1][j], // 删除
									dp[i][j - 1])); // 插入
				}
			}
		}

		// 返回结果，即 tagList1 转换成 tagList2 所需的最小编辑次数
		return dp[m][n];
	}


	/**
	 * 编辑距离算法（用于计算最相似的两个字符串）
	 * 文档地址：https://blog.csdn.net/DBC_121/article/details/104198838
	 *
	 * @param word1
	 * @param word2
	 * @return 最小编辑距离：字符串 1 通过最少多少次增删改字符的操作可以变成字符串 2
	 */
	public static int minDistance(String word1, String word2) {
		int m = word1.length();
		int n = word2.length();

		// 构建二维数组，dp[i][j] 表示将 word1 的前 i 个字符转换成 word2 的前 j 个字符所需的最小编辑次数
		int[][] dp = new int[m + 1][n + 1];

		// 初始化边界条件
		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		// 填充数组
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
					// 如果当前字符相同，则不需要编辑操作
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					// 否则，可以通过插入、删除、替换中的一种操作来将当前字符修改成目标字符
					dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j]));
				}
			}
		}

		// 返回结果，即 word1 转换成 word2 所需的最小编辑次数
		return dp[m][n];
	}
}
