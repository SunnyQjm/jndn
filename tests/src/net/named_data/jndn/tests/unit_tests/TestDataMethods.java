/**
 * Copyright (C) 2014-2015 Regents of the University of California.
 * @author: Jeff Thompson <jefft0@remap.ucla.edu>
 * From PyNDN unit-tests by Adeola Bannis.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * A copy of the GNU Lesser General Public License is in the file COPYING.
 */

package net.named_data.jndn.tests.unit_tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.named_data.jndn.ContentType;
import net.named_data.jndn.Data;
import net.named_data.jndn.KeyLocatorType;
import net.named_data.jndn.Name;
import net.named_data.jndn.Sha256WithRsaSignature;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.OnVerified;
import net.named_data.jndn.security.OnVerifyFailed;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.util.Blob;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

class CredentialStorage {
  public CredentialStorage()
  {
    Name keyName = new Name("/testname/DSK-123");
    defaultCertName_ = keyName.getSubName(0, keyName.size() - 1).append
      ("KEY").append(keyName.get(-1)).append("ID-CERT").append("0");

    Name ecdsaKeyName = new Name("/testEcdsa/DSK-123");
    ecdsaCertName_ = ecdsaKeyName.getSubName(0, ecdsaKeyName.size() - 1).append
      ("KEY").append(ecdsaKeyName.get(-1)).append("ID-CERT").append("0");

    try {
      identityStorage_.addKey
        (keyName, KeyType.RSA, new Blob(DEFAULT_RSA_PUBLIC_KEY_DER, false));
      privateKeyStorage_.setKeyPairForKeyName
        (keyName, KeyType.RSA, DEFAULT_RSA_PUBLIC_KEY_DER,
         DEFAULT_RSA_PRIVATE_KEY_DER);

      identityStorage_.addKey
        (ecdsaKeyName, KeyType.ECDSA, new Blob(DEFAULT_EC_PUBLIC_KEY_DER, false));
      privateKeyStorage_.setKeyPairForKeyName
        (ecdsaKeyName, KeyType.ECDSA, DEFAULT_EC_PUBLIC_KEY_DER,
         DEFAULT_EC_PRIVATE_KEY_DER);
    } catch (SecurityException ex) {
      // Don't expect this to happen;
      identityStorage_ = null;
      privateKeyStorage_ = null;
    }
  }

  public void
  signData(Data data, Name certificateName) throws SecurityException
  {
    if (certificateName.size() == 0)
      certificateName = defaultCertName_;
    keyChain_.sign(data, certificateName);
  }

  public void
  signData(Data data) throws SecurityException
  {
    signData(data, new Name());
  }

  public void
  signDataWithSha256(Data data) throws SecurityException
  {
    keyChain_.signWithSha256(data);
  }

  public void
  verifyData
    (Data data, OnVerified verifiedCallback, OnVerifyFailed failedCallback)
    throws SecurityException
  {
    keyChain_.verifyData(data, verifiedCallback, failedCallback);
  }

  public final Name
  getEcdsaCertName() { return ecdsaCertName_; }

  private static final ByteBuffer DEFAULT_RSA_PUBLIC_KEY_DER = TestDataMethods.toBuffer(new int[] {
    0x30, 0x82, 0x01, 0x22, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01,
    0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f, 0x00, 0x30, 0x82, 0x01, 0x0a, 0x02, 0x82, 0x01, 0x01,
    0x00, 0xb8, 0x09, 0xa7, 0x59, 0x82, 0x84, 0xec, 0x4f, 0x06, 0xfa, 0x1c, 0xb2, 0xe1, 0x38, 0x93,
    0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88, 0x1a, 0xf8, 0x25, 0x11, 0xe4, 0xfa, 0x1d, 0x61, 0x24, 0x5b,
    0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5, 0x8d, 0x54, 0xbd, 0xfb, 0x23, 0xfd, 0xe8, 0x8e,
    0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17, 0xb6, 0x05, 0xae, 0xce, 0x43, 0xbe,
    0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc, 0xc9, 0x7b, 0x62, 0x3c, 0xf5,
    0xe1, 0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb, 0x59, 0xc0, 0x94, 0x0b, 0x2c,
    0xd9, 0x7d, 0xbc, 0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0, 0x12, 0x1d, 0x42, 0x07, 0xb6,
    0x5d, 0xdb, 0xe1, 0xf6, 0xb1, 0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c, 0xa3, 0x11, 0x9b, 0xf9, 0xeb,
    0xbe, 0xb3, 0x95, 0xca, 0xa5, 0x87, 0x3f, 0x31, 0x18, 0x1a, 0xc9, 0x99, 0x01, 0xec, 0xaa, 0x90,
    0xfd, 0x8a, 0x36, 0x35, 0x5e, 0x12, 0x81, 0xbe, 0x84, 0x88, 0xa1, 0x0d, 0x19, 0x2a, 0x4a, 0x66,
    0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d, 0x3d, 0xb8, 0xd4, 0xab, 0x34, 0x90, 0x06, 0x3e, 0x1a, 0x61,
    0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d, 0x56, 0xfc, 0x83, 0xb7, 0x60, 0xc1, 0x5e, 0x9d,
    0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7, 0x4a, 0x5e, 0x18, 0xbf, 0xc0, 0x58,
    0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2, 0xdb, 0xfd, 0x72, 0x2f, 0x3c,
    0x47, 0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25, 0x20, 0x92, 0x15, 0x4b, 0x04,
    0xfe, 0x15, 0x19, 0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21, 0x91, 0x54, 0x60, 0x8b, 0x92,
    0x41, 0x02, 0x03, 0x01, 0x00, 0x01
  });

  // Java uses an unencrypted PKCS #8 PrivateKeyInfo, not a PKCS #1 RSAPrivateKey.
  private static final ByteBuffer DEFAULT_RSA_PRIVATE_KEY_DER = TestDataMethods.toBuffer(new int[] {
    0x30, 0x82, 0x04, 0xbf, 0x02, 0x01, 0x00, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7,
    0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x04, 0x82, 0x04, 0xa9, 0x30, 0x82, 0x04, 0xa5, 0x02, 0x01,
    0x00, 0x02, 0x82, 0x01, 0x01, 0x00, 0xb8, 0x09, 0xa7, 0x59, 0x82, 0x84, 0xec, 0x4f, 0x06, 0xfa,
    0x1c, 0xb2, 0xe1, 0x38, 0x93, 0x53, 0xbb, 0x7d, 0xd4, 0xac, 0x88, 0x1a, 0xf8, 0x25, 0x11, 0xe4,
    0xfa, 0x1d, 0x61, 0x24, 0x5b, 0x82, 0xca, 0xcd, 0x72, 0xce, 0xdb, 0x66, 0xb5, 0x8d, 0x54, 0xbd,
    0xfb, 0x23, 0xfd, 0xe8, 0x8e, 0xaf, 0xa7, 0xb3, 0x79, 0xbe, 0x94, 0xb5, 0xb7, 0xba, 0x17, 0xb6,
    0x05, 0xae, 0xce, 0x43, 0xbe, 0x3b, 0xce, 0x6e, 0xea, 0x07, 0xdb, 0xbf, 0x0a, 0x7e, 0xeb, 0xbc,
    0xc9, 0x7b, 0x62, 0x3c, 0xf5, 0xe1, 0xce, 0xe1, 0xd9, 0x8d, 0x9c, 0xfe, 0x1f, 0xc7, 0xf8, 0xfb,
    0x59, 0xc0, 0x94, 0x0b, 0x2c, 0xd9, 0x7d, 0xbc, 0x96, 0xeb, 0xb8, 0x79, 0x22, 0x8a, 0x2e, 0xa0,
    0x12, 0x1d, 0x42, 0x07, 0xb6, 0x5d, 0xdb, 0xe1, 0xf6, 0xb1, 0x5d, 0x7b, 0x1f, 0x54, 0x52, 0x1c,
    0xa3, 0x11, 0x9b, 0xf9, 0xeb, 0xbe, 0xb3, 0x95, 0xca, 0xa5, 0x87, 0x3f, 0x31, 0x18, 0x1a, 0xc9,
    0x99, 0x01, 0xec, 0xaa, 0x90, 0xfd, 0x8a, 0x36, 0x35, 0x5e, 0x12, 0x81, 0xbe, 0x84, 0x88, 0xa1,
    0x0d, 0x19, 0x2a, 0x4a, 0x66, 0xc1, 0x59, 0x3c, 0x41, 0x83, 0x3d, 0x3d, 0xb8, 0xd4, 0xab, 0x34,
    0x90, 0x06, 0x3e, 0x1a, 0x61, 0x74, 0xbe, 0x04, 0xf5, 0x7a, 0x69, 0x1b, 0x9d, 0x56, 0xfc, 0x83,
    0xb7, 0x60, 0xc1, 0x5e, 0x9d, 0x85, 0x34, 0xfd, 0x02, 0x1a, 0xba, 0x2c, 0x09, 0x72, 0xa7, 0x4a,
    0x5e, 0x18, 0xbf, 0xc0, 0x58, 0xa7, 0x49, 0x34, 0x46, 0x61, 0x59, 0x0e, 0xe2, 0x6e, 0x9e, 0xd2,
    0xdb, 0xfd, 0x72, 0x2f, 0x3c, 0x47, 0xcc, 0x5f, 0x99, 0x62, 0xee, 0x0d, 0xf3, 0x1f, 0x30, 0x25,
    0x20, 0x92, 0x15, 0x4b, 0x04, 0xfe, 0x15, 0x19, 0x1d, 0xdc, 0x7e, 0x5c, 0x10, 0x21, 0x52, 0x21,
    0x91, 0x54, 0x60, 0x8b, 0x92, 0x41, 0x02, 0x03, 0x01, 0x00, 0x01, 0x02, 0x82, 0x01, 0x01, 0x00,
    0x8a, 0x05, 0xfb, 0x73, 0x7f, 0x16, 0xaf, 0x9f, 0xa9, 0x4c, 0xe5, 0x3f, 0x26, 0xf8, 0x66, 0x4d,
    0xd2, 0xfc, 0xd1, 0x06, 0xc0, 0x60, 0xf1, 0x9f, 0xe3, 0xa6, 0xc6, 0x0a, 0x48, 0xb3, 0x9a, 0xca,
    0x21, 0xcd, 0x29, 0x80, 0x88, 0x3d, 0xa4, 0x85, 0xa5, 0x7b, 0x82, 0x21, 0x81, 0x28, 0xeb, 0xf2,
    0x43, 0x24, 0xb0, 0x76, 0xc5, 0x52, 0xef, 0xc2, 0xea, 0x4b, 0x82, 0x41, 0x92, 0xc2, 0x6d, 0xa6,
    0xae, 0xf0, 0xb2, 0x26, 0x48, 0xa1, 0x23, 0x7f, 0x02, 0xcf, 0xa8, 0x90, 0x17, 0xa2, 0x3e, 0x8a,
    0x26, 0xbd, 0x6d, 0x8a, 0xee, 0xa6, 0x0c, 0x31, 0xce, 0xc2, 0xbb, 0x92, 0x59, 0xb5, 0x73, 0xe2,
    0x7d, 0x91, 0x75, 0xe2, 0xbd, 0x8c, 0x63, 0xe2, 0x1c, 0x8b, 0xc2, 0x6a, 0x1c, 0xfe, 0x69, 0xc0,
    0x44, 0xcb, 0x58, 0x57, 0xb7, 0x13, 0x42, 0xf0, 0xdb, 0x50, 0x4c, 0xe0, 0x45, 0x09, 0x8f, 0xca,
    0x45, 0x8a, 0x06, 0xfe, 0x98, 0xd1, 0x22, 0xf5, 0x5a, 0x9a, 0xdf, 0x89, 0x17, 0xca, 0x20, 0xcc,
    0x12, 0xa9, 0x09, 0x3d, 0xd5, 0xf7, 0xe3, 0xeb, 0x08, 0x4a, 0xc4, 0x12, 0xc0, 0xb9, 0x47, 0x6c,
    0x79, 0x50, 0x66, 0xa3, 0xf8, 0xaf, 0x2c, 0xfa, 0xb4, 0x6b, 0xec, 0x03, 0xad, 0xcb, 0xda, 0x24,
    0x0c, 0x52, 0x07, 0x87, 0x88, 0xc0, 0x21, 0xf3, 0x02, 0xe8, 0x24, 0x44, 0x0f, 0xcd, 0xa0, 0xad,
    0x2f, 0x1b, 0x79, 0xab, 0x6b, 0x49, 0x4a, 0xe6, 0x3b, 0xd0, 0xad, 0xc3, 0x48, 0xb9, 0xf7, 0xf1,
    0x34, 0x09, 0xeb, 0x7a, 0xc0, 0xd5, 0x0d, 0x39, 0xd8, 0x45, 0xce, 0x36, 0x7a, 0xd8, 0xde, 0x3c,
    0xb0, 0x21, 0x96, 0x97, 0x8a, 0xff, 0x8b, 0x23, 0x60, 0x4f, 0xf0, 0x3d, 0xd7, 0x8f, 0xf3, 0x2c,
    0xcb, 0x1d, 0x48, 0x3f, 0x86, 0xc4, 0xa9, 0x00, 0xf2, 0x23, 0x2d, 0x72, 0x4d, 0x66, 0xa5, 0x01,
    0x02, 0x81, 0x81, 0x00, 0xdc, 0x4f, 0x99, 0x44, 0x0d, 0x7f, 0x59, 0x46, 0x1e, 0x8f, 0xe7, 0x2d,
    0x8d, 0xdd, 0x54, 0xc0, 0xf7, 0xfa, 0x46, 0x0d, 0x9d, 0x35, 0x03, 0xf1, 0x7c, 0x12, 0xf3, 0x5a,
    0x9d, 0x83, 0xcf, 0xdd, 0x37, 0x21, 0x7c, 0xb7, 0xee, 0xc3, 0x39, 0xd2, 0x75, 0x8f, 0xb2, 0x2d,
    0x6f, 0xec, 0xc6, 0x03, 0x55, 0xd7, 0x00, 0x67, 0xd3, 0x9b, 0xa2, 0x68, 0x50, 0x6f, 0x9e, 0x28,
    0xa4, 0x76, 0x39, 0x2b, 0xb2, 0x65, 0xcc, 0x72, 0x82, 0x93, 0xa0, 0xcf, 0x10, 0x05, 0x6a, 0x75,
    0xca, 0x85, 0x35, 0x99, 0xb0, 0xa6, 0xc6, 0xef, 0x4c, 0x4d, 0x99, 0x7d, 0x2c, 0x38, 0x01, 0x21,
    0xb5, 0x31, 0xac, 0x80, 0x54, 0xc4, 0x18, 0x4b, 0xfd, 0xef, 0xb3, 0x30, 0x22, 0x51, 0x5a, 0xea,
    0x7d, 0x9b, 0xb2, 0x9d, 0xcb, 0xba, 0x3f, 0xc0, 0x1a, 0x6b, 0xcd, 0xb0, 0xe6, 0x2f, 0x04, 0x33,
    0xd7, 0x3a, 0x49, 0x71, 0x02, 0x81, 0x81, 0x00, 0xd5, 0xd9, 0xc9, 0x70, 0x1a, 0x13, 0xb3, 0x39,
    0x24, 0x02, 0xee, 0xb0, 0xbb, 0x84, 0x17, 0x12, 0xc6, 0xbd, 0x65, 0x73, 0xe9, 0x34, 0x5d, 0x43,
    0xff, 0xdc, 0xf8, 0x55, 0xaf, 0x2a, 0xb9, 0xe1, 0xfa, 0x71, 0x65, 0x4e, 0x50, 0x0f, 0xa4, 0x3b,
    0xe5, 0x68, 0xf2, 0x49, 0x71, 0xaf, 0x15, 0x88, 0xd7, 0xaf, 0xc4, 0x9d, 0x94, 0x84, 0x6b, 0x5b,
    0x10, 0xd5, 0xc0, 0xaa, 0x0c, 0x13, 0x62, 0x99, 0xc0, 0x8b, 0xfc, 0x90, 0x0f, 0x87, 0x40, 0x4d,
    0x58, 0x88, 0xbd, 0xe2, 0xba, 0x3e, 0x7e, 0x2d, 0xd7, 0x69, 0xa9, 0x3c, 0x09, 0x64, 0x31, 0xb6,
    0xcc, 0x4d, 0x1f, 0x23, 0xb6, 0x9e, 0x65, 0xd6, 0x81, 0xdc, 0x85, 0xcc, 0x1e, 0xf1, 0x0b, 0x84,
    0x38, 0xab, 0x93, 0x5f, 0x9f, 0x92, 0x4e, 0x93, 0x46, 0x95, 0x6b, 0x3e, 0xb6, 0xc3, 0x1b, 0xd7,
    0x69, 0xa1, 0x0a, 0x97, 0x37, 0x78, 0xed, 0xd1, 0x02, 0x81, 0x80, 0x33, 0x18, 0xc3, 0x13, 0x65,
    0x8e, 0x03, 0xc6, 0x9f, 0x90, 0x00, 0xae, 0x30, 0x19, 0x05, 0x6f, 0x3c, 0x14, 0x6f, 0xea, 0xf8,
    0x6b, 0x33, 0x5e, 0xee, 0xc7, 0xf6, 0x69, 0x2d, 0xdf, 0x44, 0x76, 0xaa, 0x32, 0xba, 0x1a, 0x6e,
    0xe6, 0x18, 0xa3, 0x17, 0x61, 0x1c, 0x92, 0x2d, 0x43, 0x5d, 0x29, 0xa8, 0xdf, 0x14, 0xd8, 0xff,
    0xdb, 0x38, 0xef, 0xb8, 0xb8, 0x2a, 0x96, 0x82, 0x8e, 0x68, 0xf4, 0x19, 0x8c, 0x42, 0xbe, 0xcc,
    0x4a, 0x31, 0x21, 0xd5, 0x35, 0x6c, 0x5b, 0xa5, 0x7c, 0xff, 0xd1, 0x85, 0x87, 0x28, 0xdc, 0x97,
    0x75, 0xe8, 0x03, 0x80, 0x1d, 0xfd, 0x25, 0x34, 0x41, 0x31, 0x21, 0x12, 0x87, 0xe8, 0x9a, 0xb7,
    0x6a, 0xc0, 0xc4, 0x89, 0x31, 0x15, 0x45, 0x0d, 0x9c, 0xee, 0xf0, 0x6a, 0x2f, 0xe8, 0x59, 0x45,
    0xc7, 0x7b, 0x0d, 0x6c, 0x55, 0xbb, 0x43, 0xca, 0xc7, 0x5a, 0x01, 0x02, 0x81, 0x81, 0x00, 0xab,
    0xf4, 0xd5, 0xcf, 0x78, 0x88, 0x82, 0xc2, 0xdd, 0xbc, 0x25, 0xe6, 0xa2, 0xc1, 0xd2, 0x33, 0xdc,
    0xef, 0x0a, 0x97, 0x2b, 0xdc, 0x59, 0x6a, 0x86, 0x61, 0x4e, 0xa6, 0xc7, 0x95, 0x99, 0xa6, 0xa6,
    0x55, 0x6c, 0x5a, 0x8e, 0x72, 0x25, 0x63, 0xac, 0x52, 0xb9, 0x10, 0x69, 0x83, 0x99, 0xd3, 0x51,
    0x6c, 0x1a, 0xb3, 0x83, 0x6a, 0xff, 0x50, 0x58, 0xb7, 0x28, 0x97, 0x13, 0xe2, 0xba, 0x94, 0x5b,
    0x89, 0xb4, 0xea, 0xba, 0x31, 0xcd, 0x78, 0xe4, 0x4a, 0x00, 0x36, 0x42, 0x00, 0x62, 0x41, 0xc6,
    0x47, 0x46, 0x37, 0xea, 0x6d, 0x50, 0xb4, 0x66, 0x8f, 0x55, 0x0c, 0xc8, 0x99, 0x91, 0xd5, 0xec,
    0xd2, 0x40, 0x1c, 0x24, 0x7d, 0x3a, 0xff, 0x74, 0xfa, 0x32, 0x24, 0xe0, 0x11, 0x2b, 0x71, 0xad,
    0x7e, 0x14, 0xa0, 0x77, 0x21, 0x68, 0x4f, 0xcc, 0xb6, 0x1b, 0xe8, 0x00, 0x49, 0x13, 0x21, 0x02,
    0x81, 0x81, 0x00, 0xb6, 0x18, 0x73, 0x59, 0x2c, 0x4f, 0x92, 0xac, 0xa2, 0x2e, 0x5f, 0xb6, 0xbe,
    0x78, 0x5d, 0x47, 0x71, 0x04, 0x92, 0xf0, 0xd7, 0xe8, 0xc5, 0x7a, 0x84, 0x6b, 0xb8, 0xb4, 0x30,
    0x1f, 0xd8, 0x0d, 0x58, 0xd0, 0x64, 0x80, 0xa7, 0x21, 0x1a, 0x48, 0x00, 0x37, 0xd6, 0x19, 0x71,
    0xbb, 0x91, 0x20, 0x9d, 0xe2, 0xc3, 0xec, 0xdb, 0x36, 0x1c, 0xca, 0x48, 0x7d, 0x03, 0x32, 0x74,
    0x1e, 0x65, 0x73, 0x02, 0x90, 0x73, 0xd8, 0x3f, 0xb5, 0x52, 0x35, 0x79, 0x1c, 0xee, 0x93, 0xa3,
    0x32, 0x8b, 0xed, 0x89, 0x98, 0xf1, 0x0c, 0xd8, 0x12, 0xf2, 0x89, 0x7f, 0x32, 0x23, 0xec, 0x67,
    0x66, 0x52, 0x83, 0x89, 0x99, 0x5e, 0x42, 0x2b, 0x42, 0x4b, 0x84, 0x50, 0x1b, 0x3e, 0x47, 0x6d,
    0x74, 0xfb, 0xd1, 0xa6, 0x10, 0x20, 0x6c, 0x6e, 0xbe, 0x44, 0x3f, 0xb9, 0xfe, 0xbc, 0x8d, 0xda,
    0xcb, 0xea, 0x8f
  });

  private static final ByteBuffer DEFAULT_EC_PUBLIC_KEY_DER = TestDataMethods.toBuffer(new int[] {
    0x30, 0x59, 0x30, 0x13, 0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01, 0x06, 0x08, 0x2a,
    0x86, 0x48, 0xce, 0x3d, 0x03, 0x01, 0x07, 0x03, 0x42, 0x00, 0x04, 0x98, 0x9a, 0xf0, 0x61, 0x70,
    0x43, 0x2e, 0xb6, 0x12, 0x92, 0xf5, 0x57, 0x08, 0x07, 0xe7, 0xaf, 0x23, 0xab, 0x79, 0x0b, 0x05,
    0xaf, 0xa0, 0x3f, 0x8f, 0x23, 0x04, 0x50, 0xd2, 0x30, 0x47, 0x00, 0x1a, 0xff, 0x77, 0xba, 0x08,
    0x5b, 0x9a, 0xb1, 0xe6, 0x1a, 0xc4, 0x6a, 0x38, 0x00, 0x79, 0x15, 0xf8, 0x92, 0x3d, 0x9d, 0x8e,
    0x16, 0x29, 0x57, 0x34, 0x0b, 0xd4, 0x66, 0xb2, 0xe7, 0x54, 0x0b
  });

  // Java uses an unencrypted PKCS #8 PrivateKeyInfo, without full parameters.
  private static final ByteBuffer DEFAULT_EC_PRIVATE_KEY_DER = TestDataMethods.toBuffer(new int[] {
    0x30, 0x41, 0x02, 0x01, 0x00, 0x30, 0x13, 0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01,
    0x06, 0x08, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x03, 0x01, 0x07, 0x04, 0x27, 0x30, 0x25, 0x02, 0x01,
    0x01, 0x04, 0x20, 0x49, 0x35, 0xef, 0x6c, 0xbf, 0xca, 0x40, 0x55, 0xfc, 0x63, 0x61, 0x69, 0xa2,
    0x8a, 0x5d, 0x1e, 0x48, 0x7b, 0x83, 0x44, 0xf4, 0x65, 0xd3, 0xe2, 0xab, 0x2b, 0xc0, 0xbc, 0x8d,
    0x6f, 0x17, 0x1b
  });

  private MemoryIdentityStorage identityStorage_ = new MemoryIdentityStorage();
  private MemoryPrivateKeyStorage privateKeyStorage_ = new MemoryPrivateKeyStorage();
  private final KeyChain keyChain_ = new KeyChain
    (new IdentityManager(identityStorage_, privateKeyStorage_),
     new SelfVerifyPolicyManager(identityStorage_));
  private final Name defaultCertName_;
  private final Name ecdsaCertName_;
};

public class TestDataMethods {
  // Convert the int array to a ByteBuffer.
  public static ByteBuffer
  toBuffer(int[] array)
  {
    ByteBuffer result = ByteBuffer.allocate(array.length);
    for (int i = 0; i < array.length; ++i)
      result.put((byte)(array[i] & 0xff));

    result.flip();
    return result;
  }

  private static final ByteBuffer codedData = toBuffer(new int[] {
0x06, 0xCE, // NDN Data
  0x07, 0x0A, 0x08, 0x03, 0x6E, 0x64, 0x6E, 0x08, 0x03, 0x61, 0x62, 0x63, // Name
  0x14, 0x0A, // MetaInfo
    0x19, 0x02, 0x13, 0x88, // FreshnessPeriod
    0x1A, 0x04, // FinalBlockId
      0x08, 0x02, 0x00, 0x09, // NameComponent
  0x15, 0x08, 0x53, 0x55, 0x43, 0x43, 0x45, 0x53, 0x53, 0x21, // Content
  0x16, 0x28, // SignatureInfo
    0x1B, 0x01, 0x01, // SignatureType
    0x1C, 0x23, // KeyLocator
      0x07, 0x21, // Name
        0x08, 0x08, 0x74, 0x65, 0x73, 0x74, 0x6E, 0x61, 0x6D, 0x65,
        0x08, 0x03, 0x4B, 0x45, 0x59,
        0x08, 0x07, 0x44, 0x53, 0x4B, 0x2D, 0x31, 0x32, 0x33,
        0x08, 0x07, 0x49, 0x44, 0x2D, 0x43, 0x45, 0x52, 0x54,
  0x17, 0x80, // SignatureValue
    0x1A, 0x03, 0xC3, 0x9C, 0x4F, 0xC5, 0x5C, 0x36, 0xA2, 0xE7, 0x9C, 0xEE, 0x52, 0xFE, 0x45, 0xA7,
    0xE1, 0x0C, 0xFB, 0x95, 0xAC, 0xB4, 0x9B, 0xCC, 0xB6, 0xA0, 0xC3, 0x4A, 0xAA, 0x45, 0xBF, 0xBF,
    0xDF, 0x0B, 0x51, 0xD5, 0xA4, 0x8B, 0xF2, 0xAB, 0x45, 0x97, 0x1C, 0x24, 0xD8, 0xE2, 0xC2, 0x8A,
    0x4D, 0x40, 0x12, 0xD7, 0x77, 0x01, 0xEB, 0x74, 0x35, 0xF1, 0x4D, 0xDD, 0xD0, 0xF3, 0xA6, 0x9A,
    0xB7, 0xA4, 0xF1, 0x7F, 0xA7, 0x84, 0x34, 0xD7, 0x08, 0x25, 0x52, 0x80, 0x8B, 0x6C, 0x42, 0x93,
    0x04, 0x1E, 0x07, 0x1F, 0x4F, 0x76, 0x43, 0x18, 0xF2, 0xF8, 0x51, 0x1A, 0x56, 0xAF, 0xE6, 0xA9,
    0x31, 0xCB, 0x6C, 0x1C, 0x0A, 0xA4, 0x01, 0x10, 0xFC, 0xC8, 0x66, 0xCE, 0x2E, 0x9C, 0x0B, 0x2D,
    0x7F, 0xB4, 0x64, 0xA0, 0xEE, 0x22, 0x82, 0xC8, 0x34, 0xF7, 0x9A, 0xF5, 0x51, 0x12, 0x2A, 0x84,
1
  });

  static String dump(Object s1) { return s1.toString(); }
  static String dump(Object s1, Object s2) { return s1.toString() + " " + s2.toString(); }

  private static ArrayList
  dumpData(Data data)
  {
    ArrayList result = new ArrayList();

    result.add(dump("name:", data.getName().toUri()));
    if (data.getContent().size() > 0) {
      String raw = "";
      ByteBuffer buf = data.getContent().buf();
      while(buf.remaining() > 0)
        raw += (char)buf.get();
      result.add(dump("content (raw):", raw));
      result.add(dump("content (hex):", data.getContent().toHex()));
    }
    else
      result.add(dump("content: <empty>"));
    if (data.getMetaInfo().getType() != ContentType.BLOB) {
      result.add(dump("metaInfo.type:",
        data.getMetaInfo().getType() == ContentType.LINK ? "LINK" :
        (data.getMetaInfo().getType() == ContentType.KEY ? "KEY" :
         "unknown")));
    }
    result.add(dump("metaInfo.freshnessPeriod (milliseconds):",
      data.getMetaInfo().getFreshnessPeriod() >= 0 ?
        "" + data.getMetaInfo().getFreshnessPeriod() : "<none>"));
    result.add(dump("metaInfo.finalBlockId:",
      data.getMetaInfo().getFinalBlockId().getValue().size() > 0 ?
        data.getMetaInfo().getFinalBlockId().toEscapedString() : "<none>"));
    if (data.getSignature() instanceof Sha256WithRsaSignature) {
      Sha256WithRsaSignature signature = (Sha256WithRsaSignature)data.getSignature();
      result.add(dump("signature.signature:",
        signature.getSignature().size() == 0 ? "<none>" :
          signature.getSignature().toHex()));
      if (signature.getKeyLocator().getType() != KeyLocatorType.NONE) {
        if (signature.getKeyLocator().getType() ==
            KeyLocatorType.KEY_LOCATOR_DIGEST)
          result.add(dump("signature.keyLocator: KeyLocatorDigest:",
            signature.getKeyLocator().getKeyData().toHex()));
        else if (signature.getKeyLocator().getType() == KeyLocatorType.KEYNAME)
          result.add(dump("signature.keyLocator: KeyName:",
            signature.getKeyLocator().getKeyName().toUri()));
        else
          result.add(dump("signature.keyLocator: <unrecognized KeyLocatorType"));
      }
      else
        result.add(dump("signature.keyLocator: <none>"));
    }

    return result;
  }

  private static ArrayList initialDump = new ArrayList(Arrays.asList(new Object[] {
    "name: /ndn/abc",
    "content (raw): SUCCESS!",
    "content (hex): 5355434345535321",
    "metaInfo.freshnessPeriod (milliseconds): 5000.0",
    "metaInfo.finalBlockId: %00%09",
    "signature.signature: 1a03c39c4fc55c36a2e79cee52fe45a7e10cfb95acb49bccb6a0c34aaa45bfbfdf0b51d5a48bf2ab45971c24d8e2c28a4d4012d77701eb7435f14dddd0f3a69ab7a4f17fa78434d7082552808b6c4293041e071f4f764318f2f8511a56afe6a931cb6c1c0aa40110fcc866ce2e9c0b2d7fb464a0ee2282c834f79af551122a84",
    "signature.keyLocator: KeyName: /testname/KEY/DSK-123/ID-CERT"
  }));

  /**
   * Return a copy of the strings array, removing any string that start with prefix.
   */
  private static ArrayList
  removeStartingWith(ArrayList strings, String prefix)
  {
    ArrayList result = new ArrayList();
    for (int i = 0; i < strings.size(); ++i) {
      if (!((String)strings.get(i)).startsWith(prefix))
        result.add(strings.get(i));
    }

    return result;
  }

  // ignoring signature, see if two data dumps are equal
  private static boolean
  dataDumpsEqual(ArrayList dump1, ArrayList dump2)
  {
    String prefix = "signature.signature:";
    return Arrays.equals
      (removeStartingWith(dump1, prefix).toArray(),
       removeStartingWith(dump2, prefix).toArray());
  }

  private static Data
  createFreshData()
  {
    Data freshData = new Data(new Name("/ndn/abc"));
    freshData.setContent(new Blob("SUCCESS!"));
    freshData.getMetaInfo().setFreshnessPeriod(5000);
    freshData.getMetaInfo().setFinalBlockId(new Name("/%00%09").get(0));

    return freshData;
  }

  CredentialStorage credentials;
  Data freshData;

  @Before
  public void
  setUp()
  {
    // Don't show INFO log messages.
    Logger.getLogger("").setLevel(Level.WARNING);

    credentials = new CredentialStorage();
    freshData = createFreshData();
  }

  @Test
  public void
  testDump()
  {
    Data data = new Data();
    try {
      data.wireDecode(codedData);
    } catch (EncodingException ex) {
      fail("Can't decode codedData");
    }
    assertArrayEquals("Initial dump does not have expected format",
                      dumpData(data).toArray(), initialDump.toArray());
  }

  @Test
  public void
  testEncodeDecode()
  {
    Data data = new Data();
    try {
      data.wireDecode(codedData);
    } catch (EncodingException ex) {
      fail("Can't decode codedData");
    }
    // Set the content again to clear the cached encoding so we encode again.
    data.setContent(data.getContent());
    Blob encoding = data.wireEncode();

    Data reDecodedData = new Data();
    try {
      reDecodedData.wireDecode(encoding);
    } catch (EncodingException ex) {
      fail("Can't decode reDecodedData");
    }
    assertEquals("Re-decoded data does not match original dump",
                 dumpData(reDecodedData), initialDump);
  }

  @Test
  public void
  testEmptySignature()
  {
    // make sure nothing is set in the signature of newly created data
    Data data = new Data();
    Sha256WithRsaSignature signature = (Sha256WithRsaSignature)data.getSignature();
    assertTrue("Key locator type on unsigned data should not be set",
               signature.getKeyLocator().getType() == KeyLocatorType.NONE);
    assertTrue("Non-empty signature on unsigned data",
               signature.getSignature().isNull());
  }

  @Test
  public void
  testCopyFields()
  {
    Data data = new Data(freshData.getName());
    data.setContent(freshData.getContent());
    data.setMetaInfo(freshData.getMetaInfo());
    try {
      credentials.signData(data);
    } catch (SecurityException ex) {
      fail("Error calling signData" + ex.getMessage());
    }
    ArrayList freshDump = dumpData(data);
    assertTrue("Freshly created data does not match original dump",
               dataDumpsEqual(freshDump, initialDump));
  }

  @Test
  public void
  testVerify() throws SecurityException
  {
    VerifyCounter counter = new VerifyCounter();

    try {
      credentials.signData(freshData);
    } catch (SecurityException ex) {
      fail("Cannot sign freshData " + ex.getMessage());
    }

    credentials.verifyData(freshData, counter, counter);
    assertEquals("Signature verification failed", counter.onVerifyFailedCallCount_, 0);
    assertEquals("Verification callback was not used", counter.onVerifiedCallCount_, 1);
  }

  @Test
  public void
  testVerifyEcdsa() throws SecurityException
  {
    VerifyCounter counter = new VerifyCounter();

    try {
      credentials.signData(freshData, credentials.getEcdsaCertName());
    } catch (SecurityException ex) {
      fail("Cannot sign freshData " + ex.getMessage());
    }

    credentials.verifyData(freshData, counter, counter);
    assertEquals("Signature verification failed", counter.onVerifyFailedCallCount_, 0);
    assertEquals("Verification callback was not used", counter.onVerifiedCallCount_, 1);
  }

  @Test
  public void
  testVerifyDigestSha256() throws SecurityException
  {
    VerifyCounter counter = new VerifyCounter();

    try {
      credentials.signDataWithSha256(freshData);
    } catch (SecurityException ex) {
      fail("Cannot sign freshData " + ex.getMessage());
    }

    credentials.verifyData(freshData, counter, counter);
    assertEquals("Signature verification failed", counter.onVerifyFailedCallCount_, 0);
    assertEquals("Verification callback was not used", counter.onVerifiedCallCount_, 1);
  }
}
