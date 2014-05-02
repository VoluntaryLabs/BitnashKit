//
//  BNScriptSig.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNScriptSig.h"

@implementation BNScriptSig

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"programHexBytes",
                                                   @"isMultisig",
                                                   nil]];
    return self;
}

@end
