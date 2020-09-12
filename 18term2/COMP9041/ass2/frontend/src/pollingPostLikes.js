
function fetchNewData(pid, token){
    //get new post information from backend
    let getPostURL = new URL("http://127.0.0.1:5000/post/")
    getPostURL.searchParams.append("id", pid)
    return fetch(getPostURL, {
        method:"GET",
        headers:{
            "Authorization": `Token ${token}`,
        }
    })
    .then((res)=>{
        if(res.status == 200){
            return res.json()
        }else{
            return "polling fail"
        }
    })
}
function differComments(commentsList1, commentsList2){
    let different = []
    let sortedList1
    let sortedList2
    if(commentsList1.length > commentsList2.length){
        sortedList1 = commentsList1
        sortedList2 = commentsList2
    }else{
        sortedList1 = commentsList2
        sortedList2 = commentsList1
    }
    // console.log(sortedList1)
    // console.log(sortedList2)
    for (const i of sortedList1){
        let isInList2 = false
        for(const j of sortedList2){
            if (i.published == j.published){
                isInList2 = true
                break
            }
        }
        if(!isInList2){
            different.push(i)
        }
    }
    return different
}
function compare(a, b){
    return a.published - b.published
}
onmessage = function(e){
    //receive from main thread
    let data = e.data
    continuelyFetch(data.postId, data.userToken, data.currentComment)
}
function fetchNewComment(pid, token, currentComment){
    function __fetchNewComment(){
        fetchNewData(pid, token)
        .then((res)=>{
            let diffList = differComments(currentComment, res.comments)
            if(diffList){
                // console.log(diffList)
                postMessage(diffList)
            }
        })
    }
    return __fetchNewComment
}
function continuelyFetch(pid, token, currentComment){
    console.log(`current comment is ${currentComment}`)
    setInterval(fetchNewComment(pid, token, currentComment), 500)
}
onerror = function(e){
    console.log("I am not working")
    console.log(e)
}