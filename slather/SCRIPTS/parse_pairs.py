# open txt file of simulator outputs for one building distribution and write a csv file with a line per player
import sys
import csv

filename = ""
for arg in sys.argv:
    filename = arg

results = [[0 for i in range(9)] for i in range(9)]

linenum = 0;
with open(filename + ".txt") as f:
    while True:
        turns = f.readline()
        if (len(turns) < 1):
            break
        tokens1 = f.readline().split(" ")
        tokens2 = f.readline().split(" ")
        group1 = int(tokens1[1].strip("g"))
        group2 = int(tokens2[1].strip("g"))
        results[group1-1][group2-1] = int(tokens1[3])
        results[group2-1][group1-1] = int(tokens2[3])
            
with open(filename + ".csv",'w') as output:
    w = csv.writer(output)
    for row in results:
        w.writerow(row)
