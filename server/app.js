/**기본 HTTP 서버 모듈**/
const express = require('express');
const app = express();
const http = require('http');
const server = http.createServer(app);

/**소켓 통신 모듈**/
const { Server } = require("socket.io");
const io = new Server(server);

/**sql 연동 모듈**/
const mysql=require('mysql2/promise')
const crypto = require("crypto"); // mysql2/promise를 사용해야 비동기 작업이 가능하다
let connection=null

/**파일 처리 관련 모듈*/
const fs = require('fs')

/**서버 포트(3000번)**/
const PORT=process.env.PORT||3000

/**기본 index페이지 라우팅**/
// 서버가 정상 동작하는지 확인하기 위해 존재
app.get('/', (req, res) => {
    res.send('index page');
});

/**소켓 통신**/
io.on('connection', async (socket) => { // async키워드는 해당 콜백을 비동기로 처리하겠다는 것을 의미함
    console.log('socket connected');
    socket.on('disconnect',()=>{
        console.log('socket disconnected')
    })
    /**일반 로그인**/
    socket.on('id password signin', async (msg)=>{
        let returnInitValue={}
        const queryID='select * from user where userid=?'
        const hashPassword = crypto.createHash('sha512').update(msg.userPassword).digest('base64')
        const user=await connection.query(queryID,[msg.userID])

        if(user[0].length>0) {
            // 로그인 성공
            if (hashPassword == user[0][0].password) {
                // 초기 로그인
                if (user[0][0].init == 1) {
                    returnInitValue.state = 231
                    const queryUpdateInit = 'update user set init=0 where userid =?'
                    // init = 0 으로 update == 초기 로그인 완료
                    await connection.query(queryUpdateInit, [msg.userID])
                } else
                    returnInitValue.state = 232
                socket.emit('check init', returnInitValue)
            }
            // 로그인 실패
            else
                console.log('login fail')
        }
        else console.log('login fail')
    })

    socket.on('signin',async msg=>{
        const hash=crypto.createHash('sha512').update(msg).digest('base64')
        const query='select * from user where userid=?'
        const v=await connection.query(query,[hash])
        let returnValue={}
        if(v[0].length==0) {
            returnValue.success=false
        }
        else {
            returnValue.success=true
        }
        socket.emit('signin',returnValue)
    })

    socket.on('signup', async (msg) => {
        console.log('message: ' + msg)

        const hash=crypto.createHash('sha512').update(msg).digest('base64')
        const query='select * from user where userid=?'
        const v=await connection.query(query,[hash])
        let returnValue={}
        if(v[0].length==0) {
            const query2='insert into user(userid) values(?)'
            await connection.query(query2,[hash])
            returnValue.success=true
        } else {
            returnValue.success=false
        }
        socket.emit('signup',returnValue)
    })


    /*
    < 윤현조 Week 2 >
    서버 디렉토리 경로를 어떻게 설정해야 접근이 가능한 지 ... 모르겠습니다 ...
    -> 임시로 로컬 디렉토리에 저장이라도 되게 구현
    업로드되는 파일 사이즈가 너무 작음
    */
    /**프로필 사진 설정**/
    socket.on('init user image', async (msg) => {
        console.log('init user image')

        const userId = msg.userId
        const imageByteArray = msg.userImage
        const filename = userId +'.png'

        // 1. byteArray를 image(.png)로 변경 후 서버 디렉토리에 저장
        function saveImage(filename, data){
            const myBuffer = Buffer.alloc(data.length)
            for (let i = 0; i < data.length; i++) {
                myBuffer[i] = data[i]
            }
            fs.writeFile('userImages/'+filename, myBuffer, function(err) {
                if(err) {
                    console.log(err)
                } else {
                    console.log("The file was saved!")
                }
            });
        }

        await saveImage(filename, imageByteArray)

        // 2. sql 에 메타데이터 저장 -> 1번 코드가 실행되면 connection이 실행되지 않음 .. 1번 코드 없을 땐 실행되었음
        const imageURL = 'config/userImages/' + filename
        const query= 'update user set image=? where userid=?'
        connection.query(query, [imageURL, userId])
        console.log('sql update')

    })

});

server.listen(PORT, async () => {
    try {
        // await키워드를 통해 비동기 실행
        connection=await mysql.createPool({
            host: '*',
            port: '*',
            user: '*',
            password: '*',
            database: '*'
        })
    }
    catch (e) {
        console.error(e)
    }
    finally {
        console.log('listening on',PORT)
    }
});