package com.hexacta.app.model

class User (val userName:String){

  var repos: List[Repo] = List[Repo]();

  //https://api.github.com/users/cotyq/repos?type=all

  //https://api.github.com/repos/emanuelpeg/spark-scalatra/commits?author=emanuelpeg

  //https://developer.github.com/v3/repos/commits/

}
