package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 初始化
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        char[] arr = keyword.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            // 将每一个节点添加
            TrieNode subNode = tempNode.getSubNode(arr[i]);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(arr[i], subNode);
            }
            // 移动指针
            tempNode = subNode;

            // 设置结束标识
            if (i == arr.length - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 需要过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin~position这段不是敏感词
                sb.append(c);
                position = ++begin;
                // 重新将tempNode指向rootNode
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 说明begin~position这段是敏感词
                sb.append(REPLACEMENT);
                begin = ++position;
                // 重新将tempNode指向rootNode
                tempNode = rootNode;
            } else {
                position++;
            }
        }
        // 将最后一批内容加入到sb里
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否是符号
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树
    private class TrieNode {

        // 关键词结束的标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级节点的字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character key, TrieNode subNode) {
            subNodes.put(key, subNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }
    }

}
