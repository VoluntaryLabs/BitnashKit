//
//  BNError.m
//  BitnashKit
//
//  Created by Rich Collins on 4/16/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNError.h"

@implementation BNError

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"insufficientValue",
                                                   @"description",
                                                   nil]];
    return self;
}

@end
