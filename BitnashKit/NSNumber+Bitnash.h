//
//  NSNumber+Bitnash.h
//  BitnashKit
//
//  Created by Steve Dekorte on 5/11/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSNumber (Bitnash)

- (NSNumber *)btcToSatoshi;
- (NSNumber *)satoshiToBtc;

@end
