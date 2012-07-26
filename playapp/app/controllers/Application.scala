package controllers

import play.api._
import play.api.mvc._

import com.jsyn.examples._

object Application extends Controller {

  def index = Action {
    ChebyshevSong.main(null); // REMOVE THIS
    Ok(views.html.index("Your new application is ready."))
  }
  
}
