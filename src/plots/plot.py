from scipy.stats import norm
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import pandas as pd
from numpy import genfromtxt
import numpy as np


# data = pd.read_csv('../logs/echo_samples_delay.csv', sep=',', header=None)
# data.plot(kind='bar')
# plt.ylable('frequency')
# plt.xlabel('Number of packets')
# plt.title('Histogram response time')
# plt.show()

#plt.hist(rtt,histtype = 'bar', bins='auto', density=1, alpha=0.7)
# print("Length of the array is: " + str(len(rtt)))


# Response time diagram 
#x = genfromtxt('../logs/session2/echo_samples_delay.txt', delimiter=',')
#x = genfromtxt('../logs/session2/echo_samples_no_delay.txt', delimiter=',')
#x = genfromtxt('../logs/session2/echo_throughput_delay.txt', delimiter=',')
#x = genfromtxt('../logs/session2/echo_throughput_no_delay.txt', delimiter=',')
x = genfromtxt('../logs/session1/AQFsamples.txt')
#x = genfromtxt('../logs/session2/Fsamples.txt')
#x = genfromtxt('../logs/session2/Tsamples.txt')
#x = genfromtxt('../logs/session2/second_clip/aqdpcm_mean.txt')
#x = genfromtxt('../logs/session2/second_clip/aqdpcm_step.txt')
#x = genfromtxt('../logs/second_clip/aqdpcm_step.txt')
plt.subplot(2,1,1)
plt.plot(x, 'm')
plt.xlabel('Number of samples', fontsize=12)
plt.ylabel('Amplitude', fontsize=12)
plt.grid(True)

plt.subplot(2,1,2)
plt.xlabel('Number of samples', fontsize=12)
plt.ylabel('Amplitude', fontsize=12)
plt.grid(True)
plt.plot(x, 'm')
plt.xlim(100, 200)
plt.show()


#Retransmission timeout plot
# data = genfromtxt('../logs/session2/rto.txt', delimiter=' ')
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

# Copter
# data = genfromtxt('../logs/session2/copter_2nd_run/copter_info.txt', delimiter=' ')
# rtt = data[1:,0]
# srtt = data[1:,1]
# rttd = data[1:,2]
# rto = data[1:,3]
# plt.plot(rtt, label = "MOTOR")
# plt.plot(srtt, label = "ALTITUDE")
# plt.plot(rttd, label = "TEMPERATURE")
# plt.plot(rto, label = "PRESSURE")
# plt.xlabel('Number of packets', fontsize=12, labelpad=10)
# plt.ylabel("Data", fontsize=12, labelpad=10)
# plt.legend()
# plt.grid(True);
# plt.yticks(np.arange(0, 1200, 50))
# plt.show()

#Vehicle
#data = genfromtxt('../logs/session2/car_telemetry.txt', delimiter=' ')
#rtt = data[0:,0]
#srtt = data[0:,1]
#rttd = data[0:,2]
#rto = data[0:,3]
#s = data[0:, 4]
#t = data[0:, 5]
#plt.subplot(211)
#plt.plot(rtt, label = "Engine run time")
#plt.plot(rto, label = "Engine RPM")
#plt.xlabel('Number of packets', fontsize=12, labelpad=10)
#plt.ylabel("Data", fontsize=12, labelpad=10)
#plt.legend()
#plt.grid(True);
#plt.subplot(212)
#plt.plot(srtt, label = "Intake air temperature")
#plt.plot(rttd, label = "Throttle position")
#plt.plot(s, label = "Vehicle speed")
#plt.plot(t, label = "Coolant temperature")
#plt.xlabel('Number of packets', fontsize=12, labelpad=10)
#plt.ylabel("Data", fontsize=12, labelpad=10)
#plt.legend()
#plt.grid(True);
##plt.yticks(np.arange(0, 2500, 25))
#plt.show()
