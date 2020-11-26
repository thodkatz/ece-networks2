from scipy.stats import norm
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import pandas as pd
from numpy import genfromtxt


# data = pd.read_csv('../logs/echo_samples_delay.csv', sep=',', header=None)
# data.plot(kind='bar')
# plt.ylable('frequency')
# plt.xlabel('Number of packets')
# plt.title('Histogram response time')
# plt.show()

#plt.hist(rtt,histtype = 'bar', bins='auto', density=1, alpha=0.7)
# print("Length of the array is: " + str(len(rtt)))


# Response time diagram 
#x = genfromtxt('../logs/echo_samples_delay.txt', delimiter=',')
#x = genfromtxt('../logs/echo_samples_no_delay.txt', delimiter=',')
#x = genfromtxt('../logs/echo_throughput_delay.txt', delimiter=',')
#x = genfromtxt('../logs/echo_throughput_no_delay.txt', delimiter=',')
#x = genfromtxt('../logs/AQFsamples.txt')
#x = genfromtxt('../logs/Tsamples.txt')
x = genfromtxt('../logs/aqdpcm_mean.txt')
#plt.subplot(2,1,1)
plt.plot(x, 'm')
plt.xlabel('Number of samples', fontsize=12)
plt.ylabel('Mean Values', fontsize=12)
plt.grid(True)

# plt.subplot(2,1,2)
# plt.xlabel('Number of packets', fontsize=12)
# plt.ylabel('Time response', fontsize=12)
# plt.grid(True)
# plt.plot(x)
# plt.xlim(100, 200)
plt.show()


#Retransmission timeout plot
# data = genfromtxt('../logs/rto.txt', delimiter=' ')
# rtt = data[1:,0]
# srtt = data[1:,1]
# rttd = data[1:,2]
# rto = data[1:,3]
# plt.plot(rtt, label = "RTT")
# plt.plot(srtt, label = "SRTT")
# plt.plot(rttd, label = "RTTd")
# plt.plot(rto, label = "RTO")
# plt.xlabel('Number of packets', fontsize=12)
# plt.ylabel("Time response", fontsize=12)
# plt.legend()
# plt.show()
