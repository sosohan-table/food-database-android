(async ()=>{
    /**sql 연동 모듈**/
    const mysql=require('mysql2/promise')
    const fs=require('fs/promises')
    let connection=null
    try {
        connection=await mysql.createPool({
            host: '18.217.250.127',
            port: '3306',
            user: 'ssossotable',
            password: 'Mysql7968!',
            database: 'ssossotable_food'
        })
        await fs.writeFile('./sample.txt',"aaaa",'utf-8')
        const a = 'select * from user'
        const b = await connection.query(a)
        console.log(b[0])

    }
    catch (e) {
        console.log(e)
    }
    finally {
        connection.end()
    }


})()