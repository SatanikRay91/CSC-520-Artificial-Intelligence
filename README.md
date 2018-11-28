# CSC-520-Artificial-Intelligence
Coding assignments for graduate course during Fall 2018

This question concerns route-finding, with comparison of several search algorithms. This time, we're in the U.S. Here's jpg of the map below.

The solution consists of the series of cities a network packet must pass through, each city connected to one or more others by network links of the indicated length. There are no other network links.


In a language of your choice (Java or Python), implement the A* search algorithm. Your code should keep track of nodes expanded and should be able to compute the number of such nodes. Then run your algorithm on the telecom link map.

Name the source code file with the main function SearchUSA, with the file extension appropriate to the language.
The inputs should will be given through the command line, similar to Assignment 1.
In java:

% java SearchUSA searchtype srccityname destcityname

In C++:

% mv ./a.out SearchUSA
% SearchUSA searchtype srccityname destcityname

The searchtype should be either astar, greedy, or dynamic.
To save a bit of typing, the network is implemented as Prolog procedures in usroads.pl. This is a Prolog source file, and this assignment does not use Prolog. The file is provided solely as a convenience; you will have to modify it for use with your code. This time we will need the distances, and the longitude/latitude of the cities. (The percent sign (%) is a Prolog comment char, from there to end-of-line.)
The spelling of srccityname and destcityname must be the as given in usroads.pl. Do NOT change the names from lower case to upper case.
A node is said to be expanded when it is taken off a data structure and its successors generated.
Use as a heuristic the straight line distance between cities. The straight-line distance between cities is computed using decimal degrees of latitude and longitude, which also given in the file. There is a complication in computing straight line distance from longitude and latitude that arises because the earth is roughly a sphere, not a cylinder. As a guide, heuristic.pl is another Prolog file with a header comment indicating how the heuristic should work.
