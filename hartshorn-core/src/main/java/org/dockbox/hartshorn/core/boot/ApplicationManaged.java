/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.core.boot;

/**
 * An application component that is directly bound to an active {@link ApplicationManager}. This is respected
 * by the {@link ApplicationManager}, which will set itself as the component's manager when the component is
 * used by the manager.
 *
 * @see ApplicationManager
 * @see HartshornApplicationManager
 * @author Guus Lieben
 * @since 21.9
 */
public interface ApplicationManaged {

    /**
     * @return the {@link ApplicationManager} that is managing this component.
     */
    ApplicationManager applicationManager();

    /**
     * Sets the {@link ApplicationManager} that is managing this component.
     * @param applicationManager the {@link ApplicationManager} that is managing this component.
     */
    void applicationManager(ApplicationManager applicationManager);
}
