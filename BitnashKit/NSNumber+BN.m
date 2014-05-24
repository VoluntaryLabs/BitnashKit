//
//  NSNumber+Bitnash.m
//  BitnashKit
//
//  Created by Steve Dekorte on 5/11/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "NSNumber+BN.h"

@implementation NSNumber (BN)

+ (double)satoshiPerBtc
{
    return 100000000.0;
}

- (NSNumber *)btcToSatoshi
{
    long long satoshi = (long long)(self.doubleValue * self.class.satoshiPerBtc);
    return [NSNumber numberWithLongLong:satoshi];
}

- (NSNumber *)satoshiToBtc
{
    long long satoshi = self.longLongValue;
    double btc = satoshi / self.class.satoshiPerBtc;
    return [NSNumber numberWithDouble:btc];
}

@end