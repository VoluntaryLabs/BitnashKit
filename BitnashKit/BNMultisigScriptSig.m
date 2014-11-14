//
//  BNMultiScriptSig.m
//  BitnashKit
//
//  Created by Rich Collins on 3/25/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNMultisigScriptSig.h"

@implementation BNMultisigScriptSig

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:@"signatures", nil];
}

- (id)init
{
    self = [super init];
    self.signatures = [NSMutableArray array];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"signatures",
                                                   nil]];
    return self;
}

@end
