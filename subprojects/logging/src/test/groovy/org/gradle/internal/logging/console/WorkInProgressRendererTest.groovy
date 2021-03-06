/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.logging.console

import org.gradle.internal.logging.OutputSpecification
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.nativeintegration.console.ConsoleMetaData
import spock.lang.Subject

@Subject(WorkInProgressRenderer)
class WorkInProgressRendererTest extends OutputSpecification {
    private static final String IDLE = "> IDLE"
    def listener = Mock(OutputEventListener)
    def console = new ConsoleStub();
    def metaData = Mock(ConsoleMetaData);
    def renderer = new WorkInProgressRenderer(listener, console.getBuildProgressArea(), new DefaultWorkInProgressFormatter(metaData), new ConsoleLayoutCalculator(metaData))

    def setup() {
        metaData.getRows() >> 10
    }

    def "start and complete events in the same batch are ignored"() {
        when:
        renderer.onOutput([start(1, ":foo"), start(2, ":bar"), complete(1)])
        console.flush()

        then:
        progressArea.display == ["> :bar", IDLE, IDLE, IDLE]
    }

    def "events are forwarded to the listener even if are not rendered"() {
        given:
        def startEvent = start(1, ":foo")
        def completeEvent = complete(1)

        when:
        renderer.onOutput([startEvent, completeEvent])

        then:
        1 * listener.onOutput(startEvent)
        1 * listener.onOutput(completeEvent)
    }

    def "progress operation without message have no effect on progress area"() {
        when:
        renderer.onOutput([start(1)])
        console.flush()

        then:
        progressArea.display == [IDLE, IDLE, IDLE, IDLE]
    }

    def "forward the event unmodified to the listener"() {
        given:
        def event1 = event("event 1")
        def event2 = event("event 2")

        when:
        renderer.onOutput(event1)
        renderer.onOutput([event2])

        then:
        1 * listener.onOutput(event1)
        1 * listener.onOutput(event2)
        0 * _
    }

    private ConsoleStub.TestableBuildProgressTextArea getProgressArea() {
        console.buildProgressArea as ConsoleStub.TestableBuildProgressTextArea
    }
}
