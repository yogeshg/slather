The allplayers.csv file contains the results of 48 games including all 9 groups. Each parameter configuration is run with 10 different seeds, except for the n=1,d=10,t=1 which has only 8 results.

Each row of the csv file contains the scores of each group in one game ordered by group 1, group 2, etc.

Each block of 10 rows corresponds to one parameter configuration, with the blocks being in the following order:

n = 20, d = 2, t = 15
n = 1, d = 1, t = 2
n = 63, d = 2, t = 10
n = 1, d = 4, t = 4
n = 1, d = 10, t = 1

For the games with 10 results, the seeds used are in the following order, if you wish to reproduce starting positions:

[815,   906,   127,   913,   632,    98,   278,   547,   958,   965];

Unfortunately for the n=1,d=10,t=1 configuration the simulations with different seeds were being run in parallel and some of the runs evidentally crashed. I do not know which runs using which seed values actually wrote output, and these simulations took many hours to run each, so there won't be a correspondence with the seeds. Since there is only 1 player of each group initialized presumably the result is not very sensitive to the starting conditions.



The pairwise simulations are in the two files labeled by the n,d,t parameters pairs.csv
Each group played each other group exactly once, and the data is stored in a matrix where the entry on the ith row, jth column is the score group i achieved when playing against group j. The entries (i,j) and (j,i) are scores from the same one game. Note that groups did not play against themselves so the diagonal entries are all 0. 
