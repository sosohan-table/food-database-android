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
        console.log('message: ' + msg)
        const u = JSON.parse(msg)
        console.log(u.userID)
        const queryID='select * from user where userid=?'
        const user=await connection.query(queryID,[u.userID])
        const hashPassword = crypto.createHash('sha512').update(msg.userPassword).digest('base64')
        if(user[0].length>0) {
            if (hashPassword == user[0].password) { // 로그인 성공
                // 초기 로그인
                if (user[0].init == 1) {
                    returnInitValue.state = 231
                    const queryUpdateInit = 'update user set init=0 where userid =?'
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

    //device id 없을 떄
    /*socket.on('init deviceId', async (msg)=>{
        let returnInitValue={}
        const a = 'select DATE_ADD(NOW(), INTERVAL 10 MINUTE)'
        const b = connection.query(a)

        const query='insert into user_cookie value(?, ?)'
        const v=await connection.query(query,[msg.deviceID, b])

        returnInitValue.state = 231
        socket.emit('', returnInitValue)
    })

    //device id 있을 때
    socket.on('deviceId', async (msg)=>{
        let returnInitValue={}

        const a = 'select DATE_ADD(NOW(), INTERVAL 10 MINUTE)'
        const b = connection.query(a)

        const query='update user_cookie set Expire = ? where deviceId = ?'
        const v=await connection.query(query,[b, msg.deviceID])

        returnInitValue.state = 232
        socket.emit('', returnInitValue)
    })*/

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

    /**
     * 초기로그인과 자동로그인은 별개로 생각하세요
     * 1. 초기로그인일 경우 / 초기로그인이 아닐 경우 모두 쿠키 확인(check cookie)이벤트 송신하세요
     * 2. 하나의 이벤트(check cookie)로 처리하세요 (별개로 나눌 이유가 없어요)
     * 3. device id는 하나의 기기 / 한 번의 어플리케이션 설치에서는 동일하지만 그 이외의 경우에는 달라진다고 해요 (영구적인 값이 아님)
     * 3.1 즉 deviceid값을 db에 영구적으로 저장할 필요는 없어요
     * 3.2 쿠키 만료 시 deviceid값을 삭제하고 쿠키의 존재 여부는 device값을 가진 row가 있냐 없냐에 따라 처리하세요
     * **/

    socket.on('check cookie', async (msg) => {
        /**
         * TODO
         * db에서 select문을 통해 deviceId값을 검색하세요
         * 값이 있다면 232 코드
         * 값이 없다면 231 코드를 emit하세요
         * **/
        let returnInitValue={}
        //const a = 'select DATE_ADD(NOW(), INTERVAL 10 MINUTE)'
        //const b = await connection.query(a)
        const b = await connection.query('select DATE_ADD(NOW(), INTERVAL 10 MINUTE) as now')
        const now=b[0][0].now

        const query = 'select * from user_cookie where deviceId = ?;'
        const v = await connection.query(query,[msg.deviceID])

        if (v[0].length > 0) {
            const c='update user_cookie set Expire = ? where deviceId = ?'
            const d=await connection.query(c,[now, msg.deviceID])
            returnInitValue.state = 232
        }
        else {
            returnInitValue.state = 231
            //const e='insert into user_cookie value(?, ?)'
            //const v=await connection.query(e,[msg.deviceID, b])
        }
        socket.emit('check cookie', returnInitValue)

    })

    socket.on('deviceID', async (msg)=> {
        let returnInitValue = {}

        //const a = 'select DATE_ADD(NOW(), INTERVAL 10 MINUTE)'
        //const b = await connection.query(a)
        const b = await connection.query('select DATE_ADD(NOW(), INTERVAL 10 MINUTE) as now')
        const now=b[0][0].now

        const c = 'select * from user_cookie where deviceId = ?;'
        const d = await connection.query(c,[msg.deviceID])

        if (d[0].length > 0) {
            const c='update user_cookie set Expire = ? where deviceId = ?'
            const d=await connection.query(c,[now, msg.deviceID])
            //returnInitValue.state = 232
        }
        else {
            //returnInitValue.state = 231
            const e='insert into user_cookie value(?, ?)'
            const v=await connection.query(e,[msg.deviceID, now])
        }

        //returnInitValue.state = 232
        //socket.emit('deviceID', returnInitValue)

    })

    /*socket.on('check init', async (msg) => {

    })*/

    socket.on('rating list', async (msg) => {
        const query=`select food.id, name, image, userid, rating from food,rating where food.id=rating.foodid and userid=?`
        const v=await connection.query(query)
        console.log(v[0])
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