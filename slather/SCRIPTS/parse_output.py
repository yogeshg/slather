# open txt file of simulator outputs for one building distribution and write a csv file with a line per player
import sys
import csv

filename = ""
for arg in sys.argv:
    filename = arg

results = []

linenum = 0;
game = [0 for i in range(9)]
player = 0;
with open(filename + ".txt") as f:
    for line in f:        
        tokens = line.split(" ")
        if (tokens[0] == "Turns"):
            game = [0 for i in range(9)]
            player = 0
        elif len(tokens) >= 4:
            game[player] = int(tokens[3])
            player = player+1
        if (player == 9):
            results.append(game)                        
            
            
with open(filename + ".csv",'w') as output:
    w = csv.writer(output)
    for row in results:
        w.writerow(row)
