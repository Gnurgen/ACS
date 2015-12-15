clear;
close all;
numThreads = 10:5:100;
rpcThroughput =[108 186 179 143 104 94 100 107 99 74 67 67 62 43 45 42 41 40 35];
rpcLatency = [56 46 67 105 171 220 242 256 298 448 541 594 704 1034 1057 1204 1316 1442 1727];
localThroughput = [6055 18373 26349 22376 14599 21119 12320 39130 9868 13563 15376 12674 10983 13284 9787 9878 8179 7479 6916];
localLatency = [1.016 0.521 0.523 0.728 1.284 1.167 1.981 1.764 3.076 2.705 2.7 3.487 4.155 4.352 5.519 6.62 7.608 8.421 9.39];


figure
plot(numThreads, rpcLatency, numThreads, localLatency);
legend('Latency using rpc','Latency using local');
xlabel('Number of clients');
ylabel('Milliseconds per interaction');
title('Latency');

figure
plot(numThreads, rpcThroughput, numThreads, localThroughput);
legend('Throughput using rpc','Throughput using local');
xlabel('Number of clients');
ylabel('Interactions per second');
title('Throughput');

