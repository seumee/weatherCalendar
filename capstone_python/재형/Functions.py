#!/usr/bin/env python
# coding: utf-8

# setWeather(scheduleNum) 호출 -> executeSql로 schedule 정보 조회
# -> geocoding 도로명주소 좌표값 변환
# -> findaway에 좌표값 전달 -> transit_route에서 findaway로 조회한 경로에서 도보 추출
# -> 해당 도보 경로의 날씨 조회 -> DB 업로드

# In[ ]:


from geopy.geocoders import Nominatim
import requests
import pprint
from bs4 import BeautifulSoup
from datetime import *
import urllib.request, json
from sshtunnel import SSHTunnelForwarder
import pymysql
import sys


# In[ ]:


"""def transit_route"""
def transit_route(origin_lat, origin_lon, destination_lat,destination_lon):
    #Google MapsDdirections API endpoint
    endpoint = "https://maps.googleapis.com/maps/api/directions/json?"
    api_key= "AIzaSyBRRvsR_jj8_NajJiFRcIBl3_Hc-3eicOA"
    nav_request = 'origin={},{}&destination={},{}&mode=transit&transit_routing_preference=fewer_transfers&key={}'.format(origin_lat, origin_lon, destination_lat ,destination_lon,api_key)
    request = endpoint + nav_request
    #Sends the request and reads the response.
    response = urllib.request.urlopen(request).read()
    #Loads response as JSON
    directions = json.loads(response)
    return directions


# In[1]:


"""def findaway"""
def findaway(lat_beg, lon_beg, lat_fin, lon_fin):
    cord_list = []
    way = transit_route(lat_beg, lon_beg ,lat_fin, lon_fin)['routes'][0]['legs'][0]['steps']
    for i in range(len(way)):
        if way[i]['travel_mode'] == 'WALKING':
            cord_list.append(way[i]['start_location'])
    
    return cord_list


# In[ ]:


"""def getWeather"""
def getWeather(cord_list):
    part = 'hourly,minutely'
    service_key = '36afa2e28efc2803bc05866ef6460430'
    today = str(date.today()).replace('-', '')

    weather = []
    for c in cord_list:
        lat = c['lat']
        lon = c['lng']
        params = f'lat={lat}&lon={lon}&units=metric&exclude={part}&lang=kr&appid={service_key}'
        url = 'https://api.openweathermap.org/data/3.0/onecall?' + params
        res = requests.get(url)
        if res.status_code == 200:
            data = res.json()
            weather.append(data)
        else:
            print("Error Code", res.status_code)

    desc = []
    print(weather)
    desc.append(weather['current']['weather']['description'])
    for p in range(len(weather)):
        for q in range(8):
            weather_ = weather[p]['daily'][q]['weather'][0]['description']
            desc.append(weather_)

    """
    day_after = []
    for i in range(8):
        daily = data['daily'][i]['weather'][0]['description']
        if daily == "비":
            day_after.append(i)
        elif daily == "눈":
            day_after.append(i)
    """
    return desc


# In[ ]:


"""def geocoding"""
geo_local = Nominatim(user_agent = 'South Korea')

def geocoding(address):
    
    try:
        geo = geo_local.geocode(address)
        x_y = [geo.latitude, geo.longitude]
        return x_y
    
    except :
        return [0,0]


# In[ ]:


"""def executeSql"""
def executeSql(cmd):
    with SSHTunnelForwarder(('seumchae.iptime.org', 8022),
                            ssh_username='capstone',
                            ssh_password='capstone2022',
                            remote_bind_address=('127.0.0.1', 3306)) as tunnel:
        with pymysql.connect(host='127.0.0.1', user='root', password='capstone2022', port=tunnel.local_bind_port,
                             db='Capstone', charset="utf8", cursorclass=pymysql.cursors.DictCursor) as conn:
            with conn.cursor() as cur:

                sql = cmd
                cur.execute(sql)
                results = cur.fetchall()
                """
                schedule = []
                for result in results:

                    schedule.append(result)
                """
                conn.commit()
    return results


# In[ ]:


"""def setWeather"""
def setWeather(isGroup, num):
    date_result = executeSql('select appointment from schedule where num = ' + str(num))
    now = datetime.now()
    date = date_result['appointment']
    how_far = (date - now).days
    print(date)
    print(now)
    print("howfar: "+str(how_far))
    if how_far > 6:
        return 'far'
    else:
        if isGroup == True:
            #그룹날씨 업데이트
            print()
        else:
            
            """
            #get scheduleNum, personal ret from DB
            dst = executeSql('select lat, lon from schedule where num = ' + str(num))
            stt = executeSql('select lat, lon from client where email =(select email from schedule where num = {})'.format(str(num)))
            """
            
            """
            #call findaway
            return findaway(stt['lat'], stt['lon'], dst['lat'], dst['lon'])
            """


# In[ ]:


def convertGeo(tType, IDorNum):
    if tType == 'client':
        stringAddress = executeSql('select address from client where email = ' + str(IDorNum))
        
        stringAddress[0]['address']
        
        lat = geocoding(stringAddress)[0]
        lon = geocoding(stringAddress)[1]
        
        updateResult = executeSql(f'UPDATE client SET lat = {lat}, lon = {lon} where email = {str(IDorNum)}')
    elif tType == 'schedule':
        stringAddress = executeSql('select address from schedule where num = ' + str(IDorNum))

        stringAddress = stringAddress[0]['address']
        
        lat = geocoding(stringAddress)[0]
        lon = geocoding(stringAddress)[1]
        
        updateResult = executeSql(f'UPDATE schedule SET lat = {lat}, lon = {lon} WHERE num = '+str(IDorNum))

