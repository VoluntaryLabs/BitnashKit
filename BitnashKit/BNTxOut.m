//
//  BNTxOut.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTxOut.h"

@implementation BNTxOut

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:@"valueSatoshi", @"scriptPubKey", nil];
}

@end
