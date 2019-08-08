##to install keras reffer conda instal keras website 2nd method
##http://ron:python@127.0.0.1:5000/res/9
####http://ron:python@0.0.0.0:5000/res/2
#http://ron:python@192.168.43.1:5000/res/2

#http://127.0.0.1:5000/analyse?location=delhi&image=222

#pip install python-firebase
#pip install flask_httpauth
##pip install Pyrebase

from flask import Flask, render_template, request, jsonify, make_response
from flask_httpauth import HTTPBasicAuth
import pyrebase
import cv2
import numpy as np
from keras.models import load_model
from math import ceil, floor
auth = HTTPBasicAuth()
@auth.get_password
def get_password(username):
    if username == 'ron':
        return 'pycharm'
    return None
@auth.error_handler
def unauthorized():
    return make_response(jsonify({'error': 'Unauthorized access'}), 401)
app= Flask(__name__);
@app.route('/analyse')
@auth.login_required
def analyse():
    print("start")
    location=request.args.get('location',None)
    img=request.args.get('image',None)
    print("Loc",location)
    config = {
        "apiKey": "AIzaSyAiGKF8NGEKxqBGWmpd5Iyd3iuxwKkkmdw",
        "authDomain": "getphoto-e95be.firebaseapp.com",
        "databaseURL": "https://getphoto-e95be.firebaseio.com",
        "storageBucket": "getphoto-e95be.appspot.com",
        "serviceAccount": "getphoto-e95be-firebase-adminsdk-ove3z-5d2c2880f8.json"
    }
    imgu=img+'.jpg'
    firebase = pyrebase.initialize_app(config)
    storage = firebase.storage()
    db = firebase.database()
    down_name="static/"+imgu
    storage.child("images/"+img).download(down_name)
    print("Image Downloaded")
    
#####MODEL Calling
    image=cv2.imread(down_name)
    height, width = image.shape[:2]
    start_row, start_col = int(0), int(0)
    end_row, end_col = int(height * .5), int(width * .5)
    cropped_top_left = image[start_row:end_row , start_col:end_col]
    start_row, start_col = int(0),end_col
    end_row, end_col = int(height * .5), int(width)
    cropped_top_right = image[start_row:end_row , start_col:end_col]
    start_row, start_col = end_row,int(0)
    end_row, end_col = int(height), int(width * .5)
    cropped_bottom_left = image[start_row:end_row , start_col:end_col]
    start_row, start_col = start_row,end_col
    end_row, end_col = int(height), int(width)
    cropped_bottom_right = image[start_row:end_row , start_col:end_col]
    model= load_model('model_with_85%.h5')
    cropped_top_right=cv2.resize(cropped_top_right,(256,256))
    cropped_top_right=cropped_top_right/255
    cropped_top_right=cropped_top_right[:,:,::-1]
    cropped_top_right=np.expand_dims(cropped_top_right,0)
    result_cropped_top_right=model.predict(cropped_top_right)
    cropped_top_left=cv2.resize(cropped_top_left,(256,256))
    cropped_top_left=cropped_top_left/255
    cropped_top_left=cropped_top_left[:,:,::-1]
    cropped_top_left=np.expand_dims(cropped_top_left,0)
    result_cropped_top_left=model.predict(cropped_top_left)
    cropped_bottom_right=cv2.resize(cropped_bottom_right,(256,256))
    cropped_bottom_right=cropped_bottom_right/255
    cropped_bottom_right=cropped_bottom_right[:,:,::-1]
    cropped_bottom_right=np.expand_dims(cropped_bottom_right,0)
    result_cropped_bottom_right=model.predict(cropped_bottom_right)
    cropped_bottom_left=cv2.resize(cropped_bottom_left,(256,256))
    cropped_bottom_left=cropped_bottom_left/255
    cropped_bottom_left=cropped_bottom_left[:,:,::-1]
    cropped_bottom_left=np.expand_dims(cropped_bottom_left,0)
    result_cropped_bottom_left=model.predict(cropped_bottom_left)
    print("Finished predicting all four images")
    result=[[None for i in range(2)]for j in range(2)]
    result_cropped_top_right=result_cropped_top_right[0]
    index_cropped_top_right=result_cropped_top_right.argmax()
    if index_cropped_top_right==1:
        result[0][1]=0
    else:
        if index_cropped_top_right==0:
            result[0][1]=result_cropped_top_right[index_cropped_top_right]
        else:
            result[0][1]=-result_cropped_top_right[index_cropped_top_right]
    result_cropped_top_left=result_cropped_top_left[0]
    index_cropped_top_left=result_cropped_top_left.argmax()
    if index_cropped_top_left==1:
        result[0][0]=0
    else:
        if index_cropped_top_left==0:
            result[0][0]=result_cropped_top_left[index_cropped_top_left]
        else:
            result[0][0]=-result_cropped_top_left[index_cropped_top_left]
    result_cropped_bottom_right=result_cropped_bottom_right[0]
    index_cropped_bottom_right=result_cropped_bottom_right.argmax()
    if index_cropped_bottom_right==1:
        result[1][1]=0
    else:
        if index_cropped_bottom_right==0:
            result[1][1]=result_cropped_bottom_right[index_cropped_bottom_right]
        else:
            result[1][1]=-result_cropped_bottom_right[index_cropped_bottom_right]
    result_cropped_bottom_left=result_cropped_bottom_left[0]
    index_cropped_bottom_left=result_cropped_bottom_left.argmax()
    if index_cropped_bottom_left==1:
        result[1][0]=0
    else:
        if index_cropped_bottom_left==0:
            result[1][0]=result_cropped_bottom_left[index_cropped_bottom_left]
        else:
            result[1][0]=-result_cropped_bottom_left[index_cropped_bottom_left]
    print(
        "result of top right:-",result_cropped_top_right,"\n",
        "result of top left:-",result_cropped_top_left,"\n",
        "result of bottom right:-",result_cropped_bottom_right,"\n",
        "result of bottom left:-",result_cropped_bottom_left,"\n"
    )
    sum_zero=0
    sum_positive=0
    sum_negative=0
    for i in result:
        for j in i:
            if j==0:
                sum_zero+=1
            elif j>0:
                sum_positive+=j
            elif j<0:
                sum_negative+=j
    rating_p=5-sum_zero
    diff=abs((-1*sum_negative)-sum_positive)
    if diff>3.9 or diff<0.1:
        rating_d=5
    elif diff>3.4 or diff<0.6 :
        rating_d=4
    elif diff>3.0 or diff<1.1:
        rating_d=3
    elif diff>2.6 or diff<1.6:
        rating_d=2
    else:
        rating_d=1
    #rating_d_c=db.child("LOCATIONS").child(location).child("dry_wet").get().val()
    #rating_p_c=db.child("LOCATIONS").child(location).child("plastic").get().val()
    rating_d_c=db.child("LOCATIONS").child(location).child("dry_wet").get().val()
    if rating_d_c == None:
        rating_d_c=0
    rating_p_c=db.child("LOCATIONS").child(location).child("plastic").get().val()
    if rating_p_c == None:
        rating_p_c=0
    if rating_d_c>0 or rating_p_c>0:
        rating_d_u=(rating_d+rating_d_c)/2
        rating_p_u=(rating_p+rating_p_c)/2
    else:
        rating_d_u=rating_d
        rating_p_u=rating_p
    data={
            "dry_wet":rating_d_u,
            "plastic":rating_p_u
    }
    db.child("LOCATIONS").child(location).update(data)
    return jsonify({
        'ratings plastic': str(rating_p),
        'ratings dry wet': str(rating_d)
    })
if __name__=='__main__':
    app.run(host="0.0.0.0")