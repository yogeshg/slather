import csv

results = []

with open("parameter", "r") as fp:
	for setting in fp:
		setting = setting.strip().replace(' ', '.')
		f = open(setting + ".log", "r")
		turn = []
		for line in f:
			line = line.strip().split(' ')
			if len(line) == 3:
				if len(turn) > 0:
					results.append(turn)
					turn = []
			else:
				turn.append(int(line[3]))
		if len(turn) > 0:
			results.append(turn)
		f.close()

with open("result.csv", "w") as output:
	w = csv.writer(output)
	for row in results:
		w.writerow(row)
