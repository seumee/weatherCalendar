#!/usr/bin/env python
# coding: utf-8

# setWeather(scheduleNum) 호출
# --------------------------
# -> executeSql로 schedule 정보 조회
# 
#     -> geocoding 도로명주소 좌표값 변환 // 별도 처리
# 
# -> findaway에 좌표값 전달 -> transit_route에서 findaway로 조회한 경로에서 도보 추출
# 
# -> 해당 도보 경로의 날씨 조회 -> DB 업로드

# In[1]:


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


"""def WeatherOX"""
#return weatherDesc[i][targetDate] -> 날씨 좋지 않으며, 좋지 않은 원인이 되는 날씨 return
badWeathers = [201, 202, 232, 301, 302, 310, 314, 501, 502, 503, 504, 521, 
               522, 531, 601, 602, 616, 711, 731, 751, 761, 762, 771, 781,
               900, 901, 902, 956, 957, 958, 959, 960, 961, 962]
#badWeathers = ['태풍']
#badWeathers = ['비를 동반한 천둥구름', '폭우를 동반한 천둥구름']

def WeatherOX(weathers, weatherDesc, targetDate):
    for i in range(len(weathers)):
        w = weathers[i]
        if w[targetDate] in badWeathers:
            return weatherDesc[i][targetDate]
    return False


# In[2]:


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


# In[3]:


"""def findaway"""
def findaway(lat_beg, lon_beg, lat_fin, lon_fin):
    cord_list = []
    way = transit_route(lat_beg, lon_beg ,lat_fin, lon_fin)['routes'][0]['legs'][0]['steps']
    for i in range(len(way)):
        if way[i]['travel_mode'] == 'WALKING':
            cord_list.append(way[i]['start_location'])
    
    return cord_list


# In[4]:


"""def getWeather"""
def getWeather(cord):
    part = 'hourly,minutely'
    service_key = '36afa2e28efc2803bc05866ef6460430'
    today = str(date.today()).replace('-', '')

    weather = []
    lat = cord['lat']
    lon = cord['lng']
    params = f'lat={lat}&lon={lon}&units=metric&exclude={part}&lang=kr&appid={service_key}'
    url = 'https://api.openweathermap.org/data/3.0/onecall?' + params
    res = requests.get(url)
    if res.status_code == 200:
        data = res.json()
        weather.append(data)
    else:
        print("Error Code", res.status_code)

    desc = []
    id = []
    icon = []
    desc.append(weather[0]['current']['weather'][0]['description'])
    id.append(weather[0]['current']['weather'][0]['id'])
    icon.append(weather[0]['current']['weather'][0]['icon'])
    for p in range(len(weather)):
        for q in range(8):
            weather_ = weather[p]['daily'][q]['weather'][0]
            desc.append(weather_['description'])
            id.append(int(weather_['id']))
            icon.append(weather_['icon'])
    
    result_desc = weatherDateLabel(desc)
    result_id = weatherDateLabel(id)
    result_icon = weatherDateLabel(icon)

    return result_desc, result_id, result_icon 


# In[27]:


"""def weatherDateLabel"""
def weatherDateLabel(desc):
    labeled = {}
    today = date.today()
    for i in range(0, 9, 1):
        dayAfter = timedelta(days=i)
        strDayAfter = str(today + dayAfter)
        labeled[strDayAfter] = desc[i]
    return labeled


# In[5]:


"""def geocoding"""
geo_local = Nominatim(user_agent = 'South Korea')

def geocoding(address):
    
    try:
        geo = geo_local.geocode(address)
        x_y = [geo.latitude, geo.longitude]
        return x_y
    
    except :
        return [0,0]


# In[6]:


"""def executeSql"""
def executeSql(cmd):

    with pymysql.connect(host='127.0.0.1', user='root', password='capstone2022', port=3306,
                         db='Capstone', charset="utf8", cursorclass=pymysql.cursors.DictCursor) as conn:
        with conn.cursor() as cur:

            sql = cmd
            cur.execute(sql)
            results = cur.fetchall()
            conn.commit()
    return results


# In[28]:

"""def setWeather"""
#input = sys.argv[1], sys.argv[2]
#       (True/False),(scheduleNum)
def setWeather(isGroup, num):
    date_result = executeSql('select appointment from schedule where num = ' + str(num))
    now = datetime.now()
    date = date_result[0]['appointment']
    how_far = (date - now).days
    
    print(how_far)
    if how_far > 6:
        return 'date far'
    elif how_far < -1:
        return 'already passed'
    else:
        if isGroup == 'True':
            #그룹일정 날씨 업데이트
            allMemberFlag = False
            groupId = executeSql(f'select groupid from schedule where num={num}')[0]['groupid']
            emails = executeSql(f'select email from groupid where groupid=\'{groupId}\'')
            
            for e in emails:
                eInE = e['email']
                dst = executeSql('select lat, lon from schedule where num=' + str(num))
                stt = executeSql(f'select lat, lon from client where email=\'{eInE}\'')
                dst = dst[0]
                stt = stt[0]
                
                OXResult, Icon = checkPersonalRoute(dst, stt, num)
                if OXResult == False:
                    allMemberFlag = True
                    break

            ##그룹 추천일정 생성
            if OXResult != False:
                ##rmdResultCheck : 향후 9일간의 날짜를 키값, Value로 0을 가지고 있음.
                rmdResultCheck = {} 
                for i in range(9):
                    tmpDate = datetime.now() + timedelta(days=i)
                    rmdResultCheck[tmpDate.strftime('%Y-%m-%d')] = 0
                del(rmdResultCheck[date.strftime('%Y-%m-%d')])

                for e in emails:
                    eInE = e['email']
                    dst = executeSql('select lat, lon from schedule where num=' + str(num))
                    stt = executeSql(f'select lat, lon from client where email=\'{eInE}\'')
                    dst = dst[0]
                    stt = stt[0]
                    
                    rmdResult = setRecommend(dst, stt, num)
                    for r in rmdResult:
                        rmdResultCheck[r] = rmdResultCheck[r] + 1
                    
                    """
                    if rmdResult == False:
                        #추천날짜 없음
                        continue    
                    else:
                        #rmdResult == 추천날짜
                    """
                for r in rmdResultCheck:
                    #추천날짜 업데이트
                    if rmdResultCheck[r] == len(emails):
                        updateResult = executeSql(f'INSERT INTO alert (num, recommend, weather_desc) VALUES (\'{str(num)}\', \'{r}\', \'{OXResult}\')')
                        break
                #추천날짜 없음
                    updateResult = executeSql(f'INSERT INTO alert (num, recommend, weather_desc) VALUES (\'{str(num)}\', \'2001-01-01\', \'{OXResult}\')')
                        

        else:
            #개인일정 날씨 업데이트
            dst = executeSql('select lat, lon from schedule where num=' + str(num))
            stt = executeSql('select lat, lon from client where email=(select email from schedule where num={})'.format(str(num)))
            dst = dst[0]
            stt = stt[0]
            
            OXResult, Icon = checkPersonalRoute(dst, stt, num)
            allMemberFlag = not OXResult    ## DB Uploading 통일을 위해서 allMemberFlag 대입,,,
            
            if OXResult != False:
                rmdResult = setRecommend(dst, stt, num)
                if rmdResult == False:
                    #추천날짜 없음
                    updateResult = executeSql(f'INSERT INTO alert (num, recommend, weather_desc) VALUES (\'{str(num)}\', \'2001-01-01\', \'{OXResult}\')')
                else:
                    #rmdResult == 추천날짜
                    updateResult = executeSql(f'INSERT INTO alert (num, recommend, weather_desc) VALUES (\'{str(num)}\', \'{rmdResult[-1]}\', \'{OXResult}\')')
            
            """
            print("DB uploading...")
            if OXResult == False:
                #DB weather에 O
                updateResult = executeSql(f'UPDATE schedule SET weather=\'O\' WHERE num = '+str(num)) 
            else: 
                #DB weather에 X, weatherDesc에 Desc
                updateResult = executeSql(f'UPDATE schedule SET weather=\'X\' WHERE num = '+str(num))
                updateResult = executeSql(f'UPDATE schedule SET weather_desc=\'{OXResult}\' WHERE num = '+str(num))
                rmdResult = setRecommend(dst, stt, num)
                if rmdResult == False
                updateResult = executeSql(f'INSERT INTO alert () WHERE num = '+str(num))
                updateResult = executeSql(f'UPDATE alert SET recommend=\'X\' WHERE num = '+str(num))
            updateResult = executeSql(f'UPDATE schedule SET weather_icon=\'{Icon}\' WHERE num = '+str(num))
            print("DB upload complete")
            """
        
        #####################################
        print("DB uploading...")
        if allMemberFlag == False:
            #DB weather에 X, weatherDesc에 Desc
            updateResult = executeSql(f'UPDATE schedule SET weather=\'X\' WHERE num = '+str(num))
            updateResult = executeSql(f'UPDATE schedule SET weather_desc=\'{OXResult}\' WHERE num = '+str(num))

        else: #DB weather에 O
            updateResult = executeSql(f'UPDATE schedule SET weather=\'O\' WHERE num = '+str(num)) 
        updateResult = executeSql(f'UPDATE schedule SET weather_icon=\'{Icon}\' WHERE num = '+str(num))
        print("DB upload complete")
        #####################################

# In[ ]:


"""def checkPersonalRoute"""
def checkPersonalRoute(dst, stt, num):
    #call findaway
    cord_list = findaway(stt['lat'], stt['lon'], dst['lat'], dst['lon'])
    weathers = []
    for cord in cord_list:
        weathers.append(getWeather(cord))

    weatherDesc = []
    weatherId = []
    weatherIcon = []
    for w in weathers:
        weatherDesc.append(w[0])
        weatherId.append(w[1])
        weatherIcon.append(w[2])

    date = executeSql('select appointment from schedule where num=' + str(num))
    stringDate = date[0]['appointment'].strftime('%Y-%m-%d')
    
    Icon = setWorstIcon(weatherIcon, stringDate)
    OXResult = WeatherOX(weatherId, weatherDesc, stringDate)
    
    return OXResult, Icon
    
"""def setWorstIcon"""
def setWorstIcon(Icons, stringDate):
    worst = 0
    
    for Icon in Icons:
        intIcon = int(Icon[stringDate][0:2])
        if intIcon == 50:
            intIcon = 5
        if intIcon > worst:
            worst = intIcon
            stringWorst = Icon[stringDate][0:2]
    return stringWorst

"""def serRecommend"""
def setRecommend(dst, stt, num):
    weatherFlag = False
    #call findaway
    cord_list = findaway(stt['lat'], stt['lon'], dst['lat'], dst['lon'])
    weathers = []
    for cord in cord_list:
        weathers.append(getWeather(cord))

    weatherDesc = []
    weatherId = []
    for w in weathers:
        weatherDesc.append(w[0])
        weatherId.append(w[1])

    date = executeSql('select appointment from schedule where num=' + str(num))
    stringDate = date[0]['appointment'].strftime('%Y-%m-%d')
    resultRmd = []
    for curDate in (weatherId[0].keys()):
        if curDate != stringDate:
            if WeatherOX(weatherId, weatherDesc, curDate) == False:
                resultRmd.append(curDate)
    
    if not resultRmd:
        return False
    else:
        return resultRmd
        
