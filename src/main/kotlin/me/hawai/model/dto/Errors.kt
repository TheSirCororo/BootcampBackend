package me.hawai.model.dto

object Errors {
    val BodyProblem = error<Throwable>(400, 1) { "There is an error with body: $it" }
    val BadRequest = error("Формат запроса неверный!", 2, 400)
    val ClientNotFound = error("Клиента с таким ID не существует!", 3, 404)
    val AdvertiserNotFound = error("Рекламодателя с таким ID не существует!", 4, 404)
    val CampaignNotFound = error("Рекламной кампании с таким ID не существует!", 5, 404)
    val AdForbidden = error("У вас нет доступа к этой рекламе!", 6, 403)
    val ImageNotFound = error("Изображение не найдено!", 7, 404)
    val LlmDisabled = error("LLM в приложении отключено. Невозможна генерация.", 8, 403)
    val NoAds = error("Больше не осталось реклам для показа!", 10, 404)
    val OutdatedDate = error("Указана дата в прошлом.", 11, 400)

    private fun error(message: String, statusCode: Int, httpCode: Int) =
        StatusResponse.Error(message, statusCode, httpCode)

    private inline fun <reified T : Any> error(
        httpCode: Int = 400,
        statusCode: Int = 1,
        crossinline messageBuilder: (T) -> String
    ) = ErrorBuilder<T>(httpCode, statusCode, messageBuilder)
}