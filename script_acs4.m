clear;
close all;
numThreads = 10:5:100;
rpcThroughput =[108 186 179 143 104 94 100 107 99 74 67 67 62 43 45 42 41 40 35];
rpcLatency = [56 46 67 105 171 220 242 256 298 448 541 594 704 1034 1057 1204 1316 1442 1727];
localThroughput = [5070 4404 3684 2507 2151 1613 1363 1245 1049 891 848 733 659 617 562 499 443 407 389];
localLatency = [1.197 2.068 3.301 5.994 8.501 13.068 17.555 21.746 28.534 37.296 42.805 53.476 64.198 73.309 86.173 102.294 122.642 140.680 155.156];


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

