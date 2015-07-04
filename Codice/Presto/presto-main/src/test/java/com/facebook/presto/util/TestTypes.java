/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.util;

import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.type.TypeRegistry;
import org.testng.annotations.Test;

import static com.facebook.presto.util.Types.checkType;
import static org.testng.Assert.assertNull;

public class TestTypes
{
    @Test
    public void testCheckType()
            throws Exception
    {
        Number number = 123L;
        checkType(number, Long.class, "number");

        Integer integer = 123;
        checkType(integer, Integer.class, "integer");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "number must be of type java.lang.Long, not java.lang.String")
    public void testCheckTypeFails()
            throws Exception
    {
        Object number = "hello";
        checkType(number, Long.class, "number");
    }

    @Test
    public void testNonexistentType()
    {
        TypeManager typeManager = new TypeRegistry();
        assertNull(typeManager.getType("not a real type"));
    }
}
