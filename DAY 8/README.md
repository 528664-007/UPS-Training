Day 8 – Java Methods Tasks
Overview
This folder contains 11 Java programs covering fundamental programming concepts, including methods, conditional statements, loops, arrays, strings, and basic problem-solving techniques. These programs demonstrate how to write reusable methods and solve common programming problems.

Concepts and Program Summaries
1. Calculate Factorial (calculatefactorial.java)
Concepts: Methods, for loop, conditional statements, return values.

Calculates the factorial of a given non-negative integer. A method multiplies all integers from 1 to the given number and returns the result. The factorial of 0 is 1.

Example: 5! = 5 × 4 × 3 × 2 × 1 = 120

2. Check Odd or Even (checkoddoreven.java)
Concepts: Methods, if-else, modulus operator (%).

Checks whether a number is odd or even. The method uses the modulus operator to determine the remainder when the number is divided by 2. A remainder of 0 indicates an even number; otherwise, the number is odd.

Example: 7 is odd, while 8 is even.

3. Count Vowels in a String (countvowelsinstring.java)
Concepts: Strings, for loop, charAt(), toLowerCase(), conditional statements.

Counts the vowels (a, e, i, o, u) present in a given string. The program converts the string to lowercase and examines each character to identify vowels.

Example: "Hello World" contains 3 vowels.

4. Find the Maximum Element in an Array (maxelementinanarray.java)
Concepts: Arrays, methods, for loop, comparison operators.

Finds the largest element in an integer array. The method assumes the first element is the maximum and compares it with every subsequent element, updating the maximum whenever a larger value is found.

Example: [10, 25, 7, 40, 15] → Maximum: 40

5. Multiplication Table (multiplicationtable.java)
Concepts: Methods, for loop, arithmetic operators.

Prints the multiplication table of a given number from 1 to 10. A separate method performs the repeated multiplication and displays each result.

Example: For 5, the table ranges from 5 × 1 = 5 to 5 × 10 = 50.

6. Basic Java Methods (new.java)
Concepts: Method declaration, method calling, parameters, return values, void methods.

Demonstrates how to define and call methods in Java. The display() method prints a message, while the add() method accepts two integers and returns their sum.

Example: add(10, 20) returns 30.

Note: new is a reserved keyword in Java, so the source file uses a non-public class named NewDemo. The file can be compiled using javac new.java and executed using java NewDemo.

7. Palindrome Check (palindrome.java)
Concepts: Strings, while loop, charAt(), two-pointer technique, Boolean return values.

Checks whether a word or phrase reads the same forwards and backwards. Two pointers compare characters from opposite ends, moving toward the center. The implementation ignores spaces and letter case.

Example: "madam" is a palindrome; "hello" is not.

8. Reverse an Array (reversearray.java)
Concepts: Arrays, methods, while loop, temporary variables, two-pointer technique.

Reverses the order of elements in an array by swapping the first and last elements, then moving inward until the entire array is reversed.

Example: [1, 2, 3, 4, 5] → [5, 4, 3, 2, 1]

9. Find the Smallest Element in an Array (smallestelementinarr.java)
Concepts: Arrays, methods, for loop, comparison operators.

Finds the minimum element in an integer array. The method initially treats the first element as the minimum and compares it with the remaining elements, updating the minimum whenever a smaller value is found.

Example: [10, 25, 7, 40, 15] → Minimum: 7

10. Sort an Array (sortanarray.java)
Concepts: Arrays, nested for loops, conditional statements, swapping, Bubble Sort.

Sorts an integer array in ascending order using Bubble Sort. Adjacent elements are compared and swapped when they are in the wrong order. Repeated passes arrange the elements from smallest to largest.

Example: [5, 2, 8, 1, 3] → [1, 2, 3, 5, 8]

11. Sum of Two Numbers (sumof2numbers.java)
Concepts: Methods, parameters, return values, arithmetic operators, user input.

Accepts two integers from the user and passes them to a method that adds the numbers and returns the sum.

Example: 10 + 20 = 30

Key Java Concepts Practised
Methods: Reusable blocks of code that perform specific tasks.
Parameters and arguments: Values passed to methods for processing.
Return values: Results returned by methods using the return statement.
Conditional statements: if-else statements used for decision-making.
Loops: for and while loops used to repeat operations.
Arrays: Used to store and process multiple values of the same type.
String handling: Methods such as charAt(), length(), toLowerCase(), and replaceAll().
Searching and comparison: Finding the maximum and minimum elements using comparisons.
Sorting: Arranging array elements using the Bubble Sort algorithm.
User input: The Scanner class reads input from the keyboard.
Conclusion
These Day 8 practice programs strengthen the fundamentals of Java programming through practical examples. They provide experience with reusable methods, control flow, string manipulation, array operations, and elementary algorithms, building a foundation for more advanced programming and problem-solving tasks.
