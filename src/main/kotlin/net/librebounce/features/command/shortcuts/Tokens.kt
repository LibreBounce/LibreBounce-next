package net.librebounce.features.command.shortcuts

open class Token

class Literal(val literal: String) : Token()

class StatementEnd : Token()
