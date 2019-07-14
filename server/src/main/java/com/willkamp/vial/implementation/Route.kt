package com.willkamp.vial.implementation

import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

internal class Route private constructor(private val path: String, private val pathPattern: Pattern, private val keywords: List<String>) {

    fun pathPattern(): Pattern {
        return pathPattern
    }

    fun matches(path: CharSequence): Boolean {
        return pathPattern.matcher(path).matches()
    }

    fun groups(path: CharSequence): Map<String, String> {
        val matcher = pathPattern.matcher(path)
        val groups = HashMap<String, String>()
        if (matcher.matches()) {
            for (keyword in keywords) {
                groups[keyword] = matcher.group(keyword)
            }
        }
        return groups
    }

    // Let's just assume that if two Route objects have been built
    // from the same path that they will have the same pattern and
    // keywords.

    override fun equals(other: Any?): Boolean {
        return if (other === this) {
            true
        } else {
            other?.takeIf {
                it is Route && it.path == path
            }?.let {
               true
            }?: false
        }
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    companion object {

        private val keywordPattern = Pattern.compile("(:\\w+|:\\*\\w+)")

        private fun compile(pattern: String, keywords: MutableList<String>): Pattern {
            val regexPattern = StringBuilder()

            if (pattern == "/") {
                regexPattern.append("/")
            } else {
                val segments = pattern.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                for (segment in segments) {
                    if (segment != "") {
                        regexPattern.append("/")
                        if (keywordPattern.matcher(segment).matches()) {
                            var keyword = segment.substring(1)

                            if (keyword.indexOf("*") == 0) {
                                keyword = keyword.substring(1)
                                regexPattern.append("(?<").append(keyword).append(">.*)")
                            } else {
                                regexPattern.append("(?<").append(keyword).append(">[^/]*)")
                            }
                            keywords.add(keyword)
                        } else {
                            regexPattern.append(segment)
                        }
                    }
                }
            }
            regexPattern.append("[/]?")

            return Pattern.compile(regexPattern.toString())
        }

        fun build(pattern: String): Route {
            val keywords = ArrayList<String>()
            return Route(pattern, compile(pattern, keywords), keywords)
        }
    }
}
