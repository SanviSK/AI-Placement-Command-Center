package com.placement.commandcenter.seeder;

import com.placement.commandcenter.entity.DsaProblem;
import com.placement.commandcenter.repository.DsaProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DsaProblemSeeder implements CommandLineRunner {

    private final DsaProblemRepository problemRepository;

    @Override
    public void run(String... args) throws Exception {
        if (problemRepository.count() == 0) {
            System.out.println("Seeding database with real LeetCode problems...");
            List<DsaProblem> problems = Arrays.asList(
                // ARRAYS
                DsaProblem.builder()
                    .title("Two Sum")
                    .topic("Arrays")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/two-sum/")
                    .tags("array,hash-table")
                    .build(),
                DsaProblem.builder()
                    .title("Best Time to Buy and Sell Stock")
                    .topic("Arrays")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/best-time-to-buy-and-sell-stock/")
                    .tags("array,dynamic-programming")
                    .build(),
                DsaProblem.builder()
                    .title("3Sum")
                    .topic("Arrays")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/3sum/")
                    .tags("array,two-pointers,sorting")
                    .build(),
                DsaProblem.builder()
                    .title("Container With Most Water")
                    .topic("Arrays")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/container-with-most-water/")
                    .tags("array,two-pointers,greedy")
                    .build(),
                DsaProblem.builder()
                    .title("Merge Intervals")
                    .topic("Arrays")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/merge-intervals/")
                    .tags("array,sorting")
                    .build(),
                DsaProblem.builder()
                    .title("First Missing Positive")
                    .topic("Arrays")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/first-missing-positive/")
                    .tags("array,hash-table")
                    .build(),

                // STRINGS
                DsaProblem.builder()
                    .title("Valid Palindrome")
                    .topic("Strings")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/valid-palindrome/")
                    .tags("two-pointers,string")
                    .build(),
                DsaProblem.builder()
                    .title("Valid Anagram")
                    .topic("Strings")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/valid-anagram/")
                    .tags("hash-table,string,sorting")
                    .build(),
                DsaProblem.builder()
                    .title("Longest Substring Without Repeating Characters")
                    .topic("Strings")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/longest-substring-without-repeating-characters/")
                    .tags("hash-table,string,sliding-window")
                    .build(),
                DsaProblem.builder()
                    .title("Group Anagrams")
                    .topic("Strings")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/group-anagrams/")
                    .tags("hash-table,string,sorting")
                    .build(),
                DsaProblem.builder()
                    .title("Minimum Window Substring")
                    .topic("Strings")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/minimum-window-substring/")
                    .tags("hash-table,string,sliding-window")
                    .build(),

                // LINKED LISTS
                DsaProblem.builder()
                    .title("Reverse Linked List")
                    .topic("Linked Lists")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/reverse-linked-list/")
                    .tags("linked-list,recursion")
                    .build(),
                DsaProblem.builder()
                    .title("Merge Two Sorted Lists")
                    .topic("Linked Lists")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/merge-two-sorted-lists/")
                    .tags("linked-list,recursion")
                    .build(),
                DsaProblem.builder()
                    .title("Linked List Cycle")
                    .topic("Linked Lists")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/linked-list-cycle/")
                    .tags("linked-list,two-pointers,hash-table")
                    .build(),
                DsaProblem.builder()
                    .title("Remove Nth Node From End of List")
                    .topic("Linked Lists")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/remove-nth-node-from-end-of-list/")
                    .tags("linked-list,two-pointers")
                    .build(),
                DsaProblem.builder()
                    .title("Merge k Sorted Lists")
                    .topic("Linked Lists")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/merge-k-sorted-lists/")
                    .tags("linked-list,divide-and-conquer,heap-priority-queue")
                    .build(),

                // TREES
                DsaProblem.builder()
                    .title("Invert Binary Tree")
                    .topic("Trees")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/invert-binary-tree/")
                    .tags("tree,depth-first-search,breadth-first-search,binary-tree")
                    .build(),
                DsaProblem.builder()
                    .title("Maximum Depth of Binary Tree")
                    .topic("Trees")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/maximum-depth-of-binary-tree/")
                    .tags("tree,depth-first-search,breadth-first-search,binary-tree")
                    .build(),
                DsaProblem.builder()
                    .title("Binary Tree Level Order Traversal")
                    .topic("Trees")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/binary-tree-level-order-traversal/")
                    .tags("tree,breadth-first-search,binary-tree")
                    .build(),
                DsaProblem.builder()
                    .title("Validate Binary Search Tree")
                    .topic("Trees")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/validate-binary-search-tree/")
                    .tags("tree,depth-first-search,binary-search-tree,binary-tree")
                    .build(),
                DsaProblem.builder()
                    .title("Binary Tree Maximum Path Sum")
                    .topic("Trees")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/binary-tree-maximum-path-sum/")
                    .tags("dynamic-programming,tree,depth-first-search,binary-tree")
                    .build(),

                // GRAPHS
                DsaProblem.builder()
                    .title("Number of Islands")
                    .topic("Graphs")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/number-of-islands/")
                    .tags("array,depth-first-search,breadth-first-search,union-find,matrix")
                    .build(),
                DsaProblem.builder()
                    .title("Clone Graph")
                    .topic("Graphs")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/clone-graph/")
                    .tags("hash-table,depth-first-search,breadth-first-search,graph")
                    .build(),
                DsaProblem.builder()
                    .title("Course Schedule")
                    .topic("Graphs")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/course-schedule/")
                    .tags("depth-first-search,breadth-first-search,graph,topological-sort")
                    .build(),
                DsaProblem.builder()
                    .title("Network Delay Time")
                    .topic("Graphs")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/network-delay-time/")
                    .tags("depth-first-search,breadth-first-search,graph,shortest-path,heap-priority-queue")
                    .build(),

                // DYNAMIC PROGRAMMING
                DsaProblem.builder()
                    .title("Climbing Stairs")
                    .topic("DP")
                    .difficulty("EASY")
                    .leetcodeUrl("https://leetcode.com/problems/climbing-stairs/")
                    .tags("math,dynamic-programming,memoization")
                    .build(),
                DsaProblem.builder()
                    .title("Coin Change")
                    .topic("DP")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/coin-change/")
                    .tags("array,dynamic-programming,breadth-first-search")
                    .build(),
                DsaProblem.builder()
                    .title("Longest Increasing Subsequence")
                    .topic("DP")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/longest-increasing-subsequence/")
                    .tags("array,binary-search,dynamic-programming")
                    .build(),
                DsaProblem.builder()
                    .title("Edit Distance")
                    .topic("DP")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/edit-distance/")
                    .tags("string,dynamic-programming")
                    .build(),

                // GREEDY
                DsaProblem.builder()
                    .title("Jump Game")
                    .topic("Greedy")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/jump-game/")
                    .tags("array,dynamic-programming,greedy")
                    .build(),
                DsaProblem.builder()
                    .title("Gas Station")
                    .topic("Greedy")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/gas-station/")
                    .tags("array,greedy")
                    .build(),
                DsaProblem.builder()
                    .title("Non-overlapping Intervals")
                    .topic("Greedy")
                    .difficulty("MEDIUM")
                    .leetcodeUrl("https://leetcode.com/problems/non-overlapping-intervals/")
                    .tags("array,dynamic-programming,greedy,sorting")
                    .build(),
                DsaProblem.builder()
                    .title("Candy")
                    .topic("Greedy")
                    .difficulty("HARD")
                    .leetcodeUrl("https://leetcode.com/problems/candy/")
                    .tags("array,greedy")
                    .build()
            );

            problemRepository.saveAll(problems);
            System.out.println("Successfully seeded " + problems.size() + " LeetCode problems.");
        } else {
            System.out.println("DSA database already seeded. Found " + problemRepository.count() + " problems.");
        }
    }
}
