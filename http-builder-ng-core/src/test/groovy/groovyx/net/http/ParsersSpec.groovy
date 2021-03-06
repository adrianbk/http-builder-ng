/*
 * Copyright (C) 2017 David Clark
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.net.http

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.MultipartResponseContent
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.TEXT_PLAIN
import static com.stehno.ersatz.MultipartResponseContent.multipart
import static groovyx.net.http.ContentTypes.MULTIPART_MIXED

class ParsersSpec extends Specification {

    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer()

    /**
     * Example for handling multipart response content.
     */
    def 'multipart'() {
        setup:
        ersatzServer.expectations {
            get('/download') {
                responder {
                    encoder ContentType.MULTIPART_MIXED, MultipartResponseContent, Encoders.multipart
                    content multipart {
                        boundary 'abc123'
                        encoder TEXT_PLAIN, String, { o -> o as String }
                        field 'alpha', 'bravo'
                        part 'charlie', 'charlie.txt', TEXT_PLAIN, 'This is some text content'
                    }
                }
            }
        }.start()

        when:
        String result = JavaHttpBuilder.configure {
            request.uri = ersatzServer.serverUrl
        }.get {
            request.uri.path = '/download'
            response.parser(MULTIPART_MIXED[0]) { ChainedHttpConfig config, FromServer fs ->
                fs.getInputStream().text
            }
        }

        then:
        result.toString().trim().readLines() == '''
            --abc123
            Content-Disposition: form-data; name="alpha"
            Content-Type: text/plain
            
            bravo
            --abc123
            Content-Disposition: form-data; name="charlie"; filename="charlie.txt"
            Content-Type: text/plain
            
            This is some text content
            --abc123--
            '''.stripIndent().trim().readLines()
    }
}
