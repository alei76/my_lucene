package org.apache.lucene.document;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.index.FieldInfo.DocValuesType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.util.LuceneTestCase;

/** simple testcases for concrete impl of IndexableFieldType */
public class TestFieldType extends LuceneTestCase {
  
  public void testEquals() throws Exception {
    FieldType ft = new FieldType();
    assertEquals(ft, ft);
    assertFalse(ft.equals(null));
    
    FieldType ft2 = new FieldType();
    assertEquals(ft, ft2);
    assertEquals(ft.hashCode(), ft2.hashCode());
    
    FieldType ft3 = new FieldType();
    ft3.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    assertFalse(ft3.equals(ft));
    
    FieldType ft4 = new FieldType();
    ft4.setDocValueType(DocValuesType.BINARY);
    assertFalse(ft4.equals(ft));
    
    FieldType ft5 = new FieldType();
    ft5.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    assertFalse(ft5.equals(ft));
    
    FieldType ft6 = new FieldType();
    ft6.setStored(true);
    assertFalse(ft6.equals(ft));
    
    FieldType ft7 = new FieldType();
    ft7.setOmitNorms(true);
    assertFalse(ft7.equals(ft));
    
    FieldType ft8 = new FieldType();
    ft8.setNumericType(NumericType.DOUBLE);
    assertFalse(ft8.equals(ft));
    
    FieldType ft9 = new FieldType();
    ft9.setNumericPrecisionStep(3);
    assertFalse(ft9.equals(ft));
    
    FieldType ft10 = new FieldType();
    ft10.setStoreTermVectors(true);
    assertFalse(ft10.equals(ft));

  }
}
