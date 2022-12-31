#!/usr/bin/env python
# coding: utf-8

# In[ ]:


import import_ipynb


# In[1]:


from Functions_2 import *


# In[ ]:


arg1 = sys.argv[1]
arg2 = sys.argv[2]


# In[2]:


###테스트 데이터###
"""
하남->용인
개인 일정
email = seumtree@test.com
scheduleNum = 1
"""

weathers = setWeather(arg1,arg2)
"""
weatherDesc = []
weatherId = []
weatherIcon = []

for w in weathers:
    weatherDesc.append(w[0])
    weatherId.append(w[1])
    weatherIcon.append(w[2])

#print(weatherId)
"""


# cannotGo = WeatherOX(weatherId, '2022-12-17')
# print(cannotGo)

# In[ ]:




