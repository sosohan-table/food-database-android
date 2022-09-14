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
    /**event on**/
    socket.on('id password signin', async (msg)=>{
        let returnInitValue={}
        const queryID='select * from user where userid=?'
        const user=await connection.query(queryID,[msg.userID])
        const hashPassword = crypto.createHash('sha512').update(msg.userPassword).digest('base64')
        if(user[0].length>0) {
            // 로그인 성공
            if (hashPassword === user[0].password) {
                // 초기 로그인
                if (user[0].init == 1) {
                    returnInitValue.state = 231
                    const queryUpdateInit = 'update user set init=0 where userid =?'
                    connection.query(queryUpdateInit, [msg.userID])
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

    socket.on('deviceID', async (msg)=>{


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