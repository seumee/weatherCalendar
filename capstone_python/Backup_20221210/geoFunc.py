#!/usr/bin/env python
# coding: utf-8

# In[ ]:


from Functions import *

def convertGeo(tType, IDorNum):
    if tType == 'client':
        stringAddress = executeSql(f'select address from client where email = \'{IDorNum}\'')
        stringAddress = stringAddress[0]['address']
        
        lat, lon = geocoding(stringAddress)
        
        updateResult = executeSql(f'UPDATE client SET lat = {lat}, lon = {lon} where email = \'{IDorNum}\'')
        print(f"set lat, lon of ID {IDorNum} as [{lat}, {lon}]")
    elif tType == 'schedule':
        stringAddress = executeSql('select address from schedule where num = ' + str(IDorNum))
        stringAddress = stringAddress[0]['address']
        
        lat, lon = geocoding(stringAddress)
        
        updateResult = executeSql(f'UPDATE schedule SET lat = {lat}, lon = {lon} WHERE num = '+str(IDorNum))
        print(f"set lat, lon of schedule {IDorNum} as [{lat}, {lon}]")

