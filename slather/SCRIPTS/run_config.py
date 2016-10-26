# run simulator over a set of parameters, passed in as a command line argument
import os
import sys

n = sys.argv[1]
d = sys.argv[2]
t = sys.argv[3]
Seeds = [815,   906,   127,   913,   632,    98,   278,   547,   958,   965];
Players = ["g1","g2","g3","g4","g5","g6","g7","g8","g9"]

if sys.argv[4] == "all":
    players = " ".join(Players)
    for seed in Seeds:
        command = "java slather.sim.Simulator -i " + str(seed) + " -n " + n + " -d " + d + " -t " + t + " -g " + players + " 2>> " + "".join(sys.argv[1:]) + ".txt"
        print command
        os.system(command)    
elif sys.argv[4] == "pairs":
    for a in range(len(Players)):
        for b in range(a+1,len(Players)):
            for seed in Seeds:
                players = Players[a] + " " + Players[b]
                command = "java slather.sim.Simulator -i " + str(seed) + " -n " + n + " -d " + d + " -t " + t + " -g " + players + " 2>> " + "".join(sys.argv[1:]) + ".txt"
                print command
                os.system(command)
