package com.github.gkdrateit.service

import com.github.gkdrateit.config.Config
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import javax.mail.*
import javax.mail.internet.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class EmailVerificationController : CrudApiBase() {
    data class Code(val code: String, val created: Long)

    // Verification codes are not stored into databases.
    companion object {
        val props = Properties()
        val auth = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(Config.maintainerEmailAddr, Config.maintainerEmailPassword)
            }
        }
        val tempCodes = ConcurrentHashMap<String, Code>()

        init {
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = Config.maintainerEmailSmtpHostName
            props["mail.smtp.port"] = Config.maintainerEmailSmtpHostPort.toString()
        }
    }


    override val path: String
        get() = "email-verification"

    override fun handleCreate(ctx: Context): ApiResponse<*> {
        val param = ctx.paramJsonMap()
        if (param["email"] == null) {
            return missingParamError("email")
        }

        val email = param["email"]!!

        if (!transaction {
                User.find { Users.email eq email }.empty()
            }) {
            return error("User registered")
        }

        val code = (1..6).map { ('0'..'9').random() }.joinToString("")
        tempCodes[email] = Code(code, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        logger.info("Create code $code for $email")

        // Send email verification code.
        val session = Session.getInstance(props, auth)

        try {
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress(Config.maintainerEmailAddr))
            val address = arrayOf(InternetAddress(email))
            msg.setRecipients(Message.RecipientType.TO, address)
            msg.subject = "UCAS Rate It Email Verification"
            msg.addHeader("x-cloudmta-class", "standard")
            msg.setText(code)
            Transport.send(msg)
        } catch (ex: Throwable) {
            return error(ex.message!!)
        }

        return success()
    }

    override fun handleRead(ctx: Context): ApiResponse<*> {
        val param = ctx.paramJsonMap()
        arrayOf("email", "code").forEach { key ->
            if (param[key] == null) {
                return missingParamError(key)
            }
        }
        val email = param["email"]!!
        val code = param["code"]!!
        if (code != tempCodes[email]?.code) {
            return error("Wrong verification code!")
        }
        return success()
    }

    override fun handleUpdate(ctx: Context): ApiResponse<*> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<*> {
        return notImplementedError()
    }
}