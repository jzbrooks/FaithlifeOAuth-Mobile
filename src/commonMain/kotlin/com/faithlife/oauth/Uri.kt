package com.faithlife.oauth

data class Uri(
    val scheme: String?,
    val authority: String?,
    val path: String?,
    val query: String?,
) {
    companion object {
        fun parse(uri: String): Uri {
            val schemeSeparatorIndex = uri.indexOf(':').takeIf { it != -1 }

            val scheme: String? = findScheme(uri, schemeSeparatorIndex)
            val authority: String? = findAuthority(uri, schemeSeparatorIndex)
            val path: String? = findPath(uri, schemeSeparatorIndex)
            val query: String? = findQuery(uri, schemeSeparatorIndex)

            return Uri(scheme, authority, path, query)
        }

        private fun findScheme(uri: String, schemeSeparatorIndex: Int?): String? {
            if (schemeSeparatorIndex == null) return null
            return uri.take(schemeSeparatorIndex)
        }

        private fun findAuthority(uri: String, schemeSeparatorIndex: Int?): String? {
            if (schemeSeparatorIndex == null ||
                uri.length < schemeSeparatorIndex + 2 ||
                uri[schemeSeparatorIndex + 1] != '/' ||
                uri[schemeSeparatorIndex + 2] != '/'
            ) return null

            return uri.drop(schemeSeparatorIndex + 3)
                .takeWhile { char ->
                    char != '/' &&
                        char != '\\' && // path start
                        char != '?' && // query start
                        char != '#' // fragment start
                }
        }

        private fun findPath(uri: String, schemeSeparatorIndex: Int?): String? {
            if (schemeSeparatorIndex == null) return null

            val pathStart = if (uri.length < schemeSeparatorIndex + 2 ||
                uri[schemeSeparatorIndex + 1] != '/' ||
                uri[schemeSeparatorIndex + 2] != '/'
            ) {
                schemeSeparatorIndex + 1
            } else {
                var start = schemeSeparatorIndex + 3
                while (start < uri.length) {
                    when (uri[start]) {
                        '?', '#' -> return ""
                        '/', '\\' -> break
                        else -> start += 1
                    }
                }
                start
            }

            return uri.drop(pathStart)
                .takeWhile { it != '?' && it != '#' }
        }

        private fun findQuery(uri: String, schemeSeparatorIndex: Int?): String? {
            if (schemeSeparatorIndex == null) return null

            val queryStartIndex = uri.indexOf('?', schemeSeparatorIndex)
                .takeIf { it != -1 } ?: return null

            val fragmentSeparatorIndex = uri.indexOf('#', schemeSeparatorIndex)
                .takeIf { it != -1 } ?: return uri.substring(queryStartIndex + 1)

            if (fragmentSeparatorIndex < queryStartIndex) return null

            return uri.substring(queryStartIndex + 1, fragmentSeparatorIndex)
        }
    }
}
