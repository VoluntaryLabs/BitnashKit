//
//  BNReleaseTx.m
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNReleaseTx.h"

@implementation BNReleaseTx

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:
            @"inputs",
            @"outputs",
            @"hash",
            @"isLocked",
            nil];
}

@end
